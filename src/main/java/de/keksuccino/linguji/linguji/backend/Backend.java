package de.keksuccino.linguji.linguji.backend;

import com.google.common.io.Files;
import de.keksuccino.linguji.linguji.backend.brain.AbstractTranslationBrain;
import de.keksuccino.linguji.linguji.backend.brain.brains.TranslationBrains;
import de.keksuccino.linguji.linguji.backend.engine.AbstractTranslationEngine;
import de.keksuccino.linguji.linguji.backend.lib.ffmpeg.Ffmpeg;
import de.keksuccino.linguji.linguji.backend.lib.ffmpeg.info.VideoStream;
import de.keksuccino.linguji.linguji.backend.lib.mkvtoolnix.MkvToolNix;
import de.keksuccino.linguji.linguji.backend.lib.os.OSUtils;
import de.keksuccino.linguji.linguji.backend.subtitle.subtitles.AbstractSubtitle;
import de.keksuccino.linguji.linguji.backend.subtitle.translation.TranslationProcess;
import de.keksuccino.linguji.linguji.backend.engine.SharedTranslatorOptions;
import de.keksuccino.linguji.linguji.backend.engine.TranslationEngineBuilder;
import de.keksuccino.linguji.linguji.backend.lib.FileUtils;
import de.keksuccino.linguji.linguji.backend.lib.lang.Locale;
import de.keksuccino.linguji.linguji.backend.lib.logger.LogHandler;
import de.keksuccino.linguji.linguji.backend.lib.logger.SimpleLogger;
import de.keksuccino.linguji.linguji.frontend.Frontend;
import de.keksuccino.linguji.linguji.frontend.TaskExecutor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class Backend {

    private static final SimpleLogger LOGGER = LogHandler.getLogger();
    public static final String VERSION = "1.4.0";
    public static final File TEMP_DIRECTORY = new File("temp_data");

    private static Options options;

    public static void init() {

        Backend.resetTempDirectory();

        Locale.registerCustomLocals();

        updateOptions();

        //Create input and output directories
        if (!getOptions().inputDirectory.getValue().trim().isEmpty()) {
            FileUtils.createDirectory(new File(getOptions().inputDirectory.getValue()), false);
        }
        if (!getOptions().outputDirectory.getValue().trim().isEmpty()) {
            FileUtils.createDirectory(new File(getOptions().outputDirectory.getValue()), false);
        }

        FileUtils.createDirectory(Ffmpeg.DEFAULT_FFMPEG_DIRECTORY, false);
        FileUtils.createDirectory(MkvToolNix.DEFAULT_MKVTOOLNIX_DIRECTORY, false);

    }

    @NotNull
    public static TranslationProcess translate(@Nullable VideoStream videoFileSubtitleStream) {

        Backend.resetTempDirectory();

        final TranslationProcess process = new TranslationProcess();
        final File tempExtractedSubtitleDir = FileUtils.createDirectory(new File(TEMP_DIRECTORY, "extracted_subs_" + System.currentTimeMillis()), false);

        Thread t = new Thread(() -> {

            boolean finishedWithoutErrors = true;

            try {

                Locale sourceLang = Locale.getByName(Backend.getOptions().sourceLanguageLocale.getValue());
                Locale targetLang = Locale.getByName(Backend.getOptions().targetLanguageLocale.getValue());
                if ((sourceLang == null) || (targetLang == null)) throw new IllegalArgumentException("Source or target language invalid! Needs to be a valid language!");

                TranslationEngineBuilder<?> primaryBuilder = Objects.requireNonNull(SharedTranslatorOptions.getPrimaryTranslationEngine());
                TranslationEngineBuilder<?> fallbackBuilder = Objects.requireNonNull(SharedTranslatorOptions.getFallbackTranslationEngine());
                AbstractTranslationEngine primaryEngine = Objects.requireNonNull(primaryBuilder.createInstance());
                AbstractTranslationEngine fallbackEngine = Objects.requireNonNull(fallbackBuilder.createInstance());

                String inDirString = Backend.getOptions().inputDirectory.getValue();
                String outDirString = Backend.getOptions().outputDirectory.getValue();
                if (inDirString.trim().isEmpty() || outDirString.trim().isEmpty()) throw new FileNotFoundException("Input or output directory is empty! Needs to be a valid directory!");
                File outDir = FileUtils.createDirectory(new File(outDirString), false);

                List<File> inputFiles = getInputFiles();
                List<AbstractSubtitle> subtitles = new ArrayList<>();

                if (inputFiles.isEmpty()) {
                    TaskExecutor.queueTask(() -> {
                        Frontend.openAlert(
                                "Empty input directory!",
                                "No files found in the input directory!",
                                "Linguji did not find any files in the input directory!\nIf there are files in that directory, make sure Linguji has read/write access.");
                    });
                    LOGGER.warn("Input directory is empty or the system failed to get the input files!");
                    process.running = false;
                    return;
                }

                //Add subtitles of video files from input directory (video support Windows-only for now)
                if ((videoFileSubtitleStream != null) && OSUtils.isWindows()) {
                    MkvToolNix mkvToolNix = MkvToolNix.buildDefault();
                    for (File videoFile : getVideoInputFiles()) {
                        File extractedSubtitle = mkvToolNix.extractSubtitleTrackFromMkv(videoFile, videoFileSubtitleStream, tempExtractedSubtitleDir);
                        LOGGER.info("Extracted subtitle track from: " + videoFile.getPath());
                        addToListIfValidSubtitle(extractedSubtitle, videoFile, subtitles);
                    }
                }

                //Add normal subtitle files from input directory
                for (File file : inputFiles) {
                    addToListIfValidSubtitle(file, null, subtitles);
                }

                if (subtitles.isEmpty()) {
                    TaskExecutor.queueTask(() -> {
                        Frontend.openAlert(
                                "No files found!",
                                "No translatable files found!",
                                "There are no translatable files in the input directory!\nMake sure all input files are supported by Linguji!");
                    });
                    LOGGER.warn("No translatable files found in input directory!");
                    process.running = false;
                    return;
                }

                process.subtitles = new ArrayList<>(subtitles);

                for (AbstractSubtitle subtitle : subtitles) {
                    if (!process.running) break;
                    process.currentSubtitle = subtitle;
                    process.currentSubtitleFinishedLines.clear();
                    process.currentSubtitleTranslatableLinesCount = 0;
                    try {

                        for (AbstractTranslationBrain<?> brain : TranslationBrains.getBrains()) {
                            if (brain.checkSubtitleCompatibility(subtitle)) {
                                brain.translate(subtitle, process, primaryEngine, fallbackEngine);
                                subtitle.translationFinishStatus = AbstractSubtitle.TranslationFinishStatus.FINISHED;
                                break;
                            }
                        }

                        if (!process.running) break;

                        //Write translated subtitle file if FINISHED
                        if (subtitle.translationFinishStatus == AbstractSubtitle.TranslationFinishStatus.FINISHED) {
                            Objects.requireNonNull(subtitle.sourceFile, "Source file of subtitle was NULL!");
                            String fileExtension = Files.getFileExtension(subtitle.sourceFile.getPath()).replace(".", "");
                            String fileName = Files.getNameWithoutExtension(subtitle.sourceFile.getPath());
                            String outFileSuffix = Backend.getOptions().outputFileSuffix.getValue();
                            File outSubtitleFile = (subtitle.sourceVideoFile == null) ? new File(outDir, fileName + outFileSuffix + "." + fileExtension) : new File(tempExtractedSubtitleDir, fileName + "_translated." + fileExtension);
                            FileUtils.writeTextToFile(outSubtitleFile, subtitle.serialize());
                            if (subtitle.sourceVideoFile != null) {
                                MkvToolNix mkvToolNix = MkvToolNix.buildDefault();
                                String videoFileExtension = Files.getFileExtension(subtitle.sourceVideoFile.getPath()).replace(".", "");
                                String videoFileName = Files.getNameWithoutExtension(subtitle.sourceVideoFile.getPath());
                                File outVideoFile = new File(outDir, videoFileName + outFileSuffix + "." + videoFileExtension);
                                mkvToolNix.addSubtitleToMkv(subtitle.sourceVideoFile, outVideoFile, outSubtitleFile, targetLang, Backend.getOptions().setVideoSubtitleAsDefault.getValue());
                                LOGGER.info("Translation of video file subtitle finished and video saved to: " + outVideoFile.getAbsolutePath());
                            } else {
                                LOGGER.info("Translation of subtitle file successfully finished and saved to: " + outSubtitleFile.getAbsolutePath());
                            }
                        } else {
                            finishedWithoutErrors = false;
                        }

                    } catch (Exception ex) {
                        finishedWithoutErrors = false;
                        subtitle.translationFinishStatus = AbstractSubtitle.TranslationFinishStatus.FINISHED_WITH_EXCEPTIONS;
                        LOGGER.error("Failed to finish translation of subtitle file: " + ((subtitle.sourceFile != null) ? subtitle.sourceFile.getAbsolutePath() : "NULL"), ex);
                    }
                    process.finishedSubtitles.add(subtitle);
                }

            } catch (Exception ex) {
                finishedWithoutErrors = false;
                LOGGER.error("Error while trying to translate subtitles!", ex);
            }

            process.running = false;

            if (!process.stoppedByUser) {

                final boolean finalFinishedWithoutErrors = finishedWithoutErrors;
                TaskExecutor.queueTask(() -> {

                    if (finalFinishedWithoutErrors) {

                        Frontend.openAlert(
                                "Finished!",
                                "Translation process successfully finished!",
                                "The translation process finished without errors!\nYou can find the translated files in the output directory.");

                    } else {

                        Frontend.openAlert(
                                "Finished with errors!",
                                "Translation process finished with errors!",
                                "Errors happened during the translation process!\nNot all files could be translated!\n\nPlease check the log file for more information.");

                    }

                });

            }

        }, "Backend Translation Thread");

        t.setDaemon(true);
        t.start();

        return process;

    }

    protected static void addToListIfValidSubtitle(@NotNull File file, @Nullable File videoSource, @NotNull List<AbstractSubtitle> subtitles) {

        AbstractSubtitle subtitle = null;

        for (AbstractTranslationBrain<?> brain : TranslationBrains.getBrains()) {
            if (brain.checkFileCompatibility(file)) {
                subtitle = Objects.requireNonNull(brain.parseFile(file), "Failed to parse " + brain.getDisplayName() + " file: " + file.getAbsolutePath());
                break;
            }
        }

        if (subtitle != null) {
            subtitle.sourceFile = file;
            subtitle.sourceVideoFile = videoSource;
            subtitles.add(subtitle);
        }

    }

    @NotNull
    public static List<File> getInputFiles() {
        List<File> files = new ArrayList<>();
        String inDirString = Backend.getOptions().inputDirectory.getValue();
        File inDir = FileUtils.createDirectory(new File(inDirString), false);
        File[] filesInInput = inDir.listFiles();
        if (filesInInput != null) files.addAll(Arrays.asList(filesInInput));
        return files;
    }

    @NotNull
    public static List<File> getVideoInputFiles() {
        List<File> videoFiles = new ArrayList<>();
        for (File f : getInputFiles()) {
            if (f.isFile()) {
                //MKV
                if (f.getAbsolutePath().toLowerCase().endsWith(".mkv")) videoFiles.add(f);
            }
        }
        return videoFiles;
    }

    @Nullable
    public static File getFirstVideoOfInputDirectory() {
        List<File> videoFiles = getVideoInputFiles();
        if (!videoFiles.isEmpty()) return videoFiles.get(0);
        return null;
    }

    public static void resetTempDirectory() {
        try {
            org.apache.commons.io.FileUtils.deleteDirectory(TEMP_DIRECTORY);
            FileUtils.createDirectory(TEMP_DIRECTORY, false);
        } catch (Exception ex) {
            LOGGER.error("Failed to clear temp directory!", ex);
        }
    }

    public static void updateOptions() {
        options = new Options();
    }

    @NotNull
    public static Options getOptions() {
        if (options == null) updateOptions();
        return options;
    }

}
