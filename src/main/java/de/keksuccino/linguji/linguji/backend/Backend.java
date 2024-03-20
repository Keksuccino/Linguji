package de.keksuccino.linguji.linguji.backend;

import com.google.common.io.Files;
import de.keksuccino.linguji.linguji.backend.subtitle.subtitles.AbstractSubtitle;
import de.keksuccino.linguji.linguji.backend.subtitle.subtitles.AssSubtitle;
import de.keksuccino.linguji.linguji.backend.subtitle.translation.SubtitleTranslator;
import de.keksuccino.linguji.linguji.backend.subtitle.translation.TranslationProcess;
import de.keksuccino.linguji.linguji.backend.translator.gemini.GeminiTranslationEngine;
import de.keksuccino.linguji.linguji.backend.util.FileUtils;
import de.keksuccino.linguji.linguji.backend.util.lang.Locale;
import de.keksuccino.linguji.linguji.backend.util.logger.LogHandler;
import de.keksuccino.linguji.linguji.backend.util.logger.SimpleLogger;
import org.jetbrains.annotations.NotNull;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Backend {

    //TODO wenn fallback, dann line packets in SubtitleTranslator kleiner machen
    // - Max 2 Zeilen pro packet
    // - Line packet wird translatePacket() mitgegeben, also einfach in sub-packs splitten

    //TODO In GUI fixen: Gemini Fallback toggle zu allgemeinem "Use Fallback" toggle ändern

    private static final SimpleLogger LOGGER = LogHandler.getLogger();
    public static final String VERSION = "1.0.1";

    private static Options options;

    public static void init() {

        updateOptions();

        //Create input and output directories
        if (!getOptions().inputDirectory.getValue().trim().isEmpty()) {
            FileUtils.createDirectory(new File(getOptions().inputDirectory.getValue()), false);
        }
        if (!getOptions().outputDirectory.getValue().trim().isEmpty()) {
            FileUtils.createDirectory(new File(getOptions().outputDirectory.getValue()), false);
        }

    }

    @NotNull
    public static TranslationProcess translate() {

        TranslationProcess process = new TranslationProcess();

        Thread t = new Thread(() -> {

            try {

                Locale sourceLang = Locale.getByName(Backend.getOptions().sourceLanguageLocale.getValue());
                Locale targetLang = Locale.getByName(Backend.getOptions().targetLanguageLocale.getValue());
                if ((sourceLang == null) || (targetLang == null)) throw new IllegalArgumentException("Source or target language invalid! Needs to be a valid language!");

                GeminiTranslationEngine geminiTranslationEngine = new GeminiTranslationEngine(Backend.getOptions().geminiApiKey.getValue(), Backend.getOptions().aiPrompt.getValue(), sourceLang, targetLang);
                SubtitleTranslator<AssSubtitle> geminiAssSubtitleTranslator = new SubtitleTranslator<>(geminiTranslationEngine, false);

                String inDirString = Backend.getOptions().inputDirectory.getValue();
                String outDirString = Backend.getOptions().outputDirectory.getValue();
                if (inDirString.trim().isEmpty() || outDirString.trim().isEmpty()) throw new FileNotFoundException("Input or output directory is empty! Needs to be a valid directory!");
                File inDir = FileUtils.createDirectory(new File(inDirString), false);
                File outDir = FileUtils.createDirectory(new File(outDirString), false);

                List<AbstractSubtitle> subtitles = new ArrayList<>();
                for (File file : Objects.requireNonNull(inDir.listFiles(), "Failed to get subtitle files from input directory!")) {
                    //Handle ASS subtitle files
                    if (file.isFile() && file.getPath().toLowerCase().endsWith(".ass")) {
                        AssSubtitle assSubtitle = Objects.requireNonNull(AssSubtitle.create(file), "Failed to parse ASS subtitle file: " + file.getAbsolutePath());
                        assSubtitle.sourceFile = file;
                        subtitles.add(assSubtitle);
                    }
                }

                process.subtitles = new ArrayList<>(subtitles);

                for (AbstractSubtitle subtitle : subtitles) {
                    if (!process.running) break;
                    process.currentSubtitle = subtitle;
                    process.currentSubtitleFinishedLines.clear();
                    process.currentSubtitleTranslatableLinesCount = 0;
                    try {
                        //Handle ASS subtitles
                        if (subtitle instanceof AssSubtitle assSubtitle) {
                            geminiAssSubtitleTranslator.translate(assSubtitle, process);
                            subtitle.translationFinishStatus = AbstractSubtitle.TranslationFinishStatus.FINISHED;
                        }
                        if (!process.running) break;
                        //Write translated subtitle file if FINISHED
                        if (subtitle.translationFinishStatus == AbstractSubtitle.TranslationFinishStatus.FINISHED) {
                            Objects.requireNonNull(subtitle.sourceFile, "Source file of subtitle was NULL!");
                            String fileExtension = Files.getFileExtension(subtitle.sourceFile.getPath()).replace(".", "");
                            String fileName = Files.getNameWithoutExtension(subtitle.sourceFile.getPath());
                            String outFileSuffix = Backend.getOptions().outputFileSuffix.getValue();
                            File translateOutFile = new File(outDir, fileName + outFileSuffix + "." + fileExtension);
                            FileUtils.writeTextToFile(translateOutFile, subtitle.serialize());
                            LOGGER.info("Translation of file successfully finished and saved to: " + translateOutFile.getAbsolutePath());
                        }
                    } catch (Exception ex) {
                        subtitle.translationFinishStatus = AbstractSubtitle.TranslationFinishStatus.FINISHED_WITH_EXCEPTIONS;
                        LOGGER.error("Failed to finish translation of subtitle file: " + ((subtitle.sourceFile != null) ? subtitle.sourceFile.getAbsolutePath() : "NULL"), ex);
                    }
                    process.finishedSubtitles.add(subtitle);
                }

            } catch (Exception ex) {
                LOGGER.error("Error while trying to translate subtitles!", ex);
            }

            process.running = false;

        }, "Backend Translation Thread");

        t.setDaemon(true);
        t.start();

        return process;

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
