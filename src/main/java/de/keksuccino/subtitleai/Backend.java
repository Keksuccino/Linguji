package de.keksuccino.subtitleai;

import com.google.common.io.Files;
import de.keksuccino.subtitleai.translator.gemini.GeminiTranslationEngine;
import de.keksuccino.subtitleai.subtitle.subtitles.AbstractSubtitle;
import de.keksuccino.subtitleai.subtitle.subtitles.AssSubtitle;
import de.keksuccino.subtitleai.subtitle.translation.SubtitleTranslator;
import de.keksuccino.subtitleai.subtitle.translation.TranslationProcess;
import de.keksuccino.subtitleai.util.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Backend {

    private static final Logger LOGGER = LogManager.getLogger();

    @NotNull
    public static TranslationProcess translate() {

        TranslationProcess process = new TranslationProcess();

        new Thread(() -> {

            try {

                GeminiTranslationEngine geminiTranslationEngine = new GeminiTranslationEngine(Main.getOptions().geminiApiKey.getValue(), Main.getOptions().aiPrompt.getValue());
                SubtitleTranslator<AssSubtitle> geminiAssSubtitleTranslator = new SubtitleTranslator<>(geminiTranslationEngine, false);

                String inDirString = Main.getOptions().inputDirectory.getValue();
                String outDirString = Main.getOptions().outputDirectory.getValue();
                if (inDirString.trim().isEmpty() || outDirString.trim().isEmpty()) throw new FileNotFoundException("Input or output directory is empty! Needs to be a valid directory!");
                File inDir = FileUtils.createDirectory(new File(inDirString), false);
                File outDir = FileUtils.createDirectory(new File(outDirString), false);
                String sourceLang = Main.getOptions().sourceLanguage.getValue();
                String targetLang = Main.getOptions().targetLanguage.getValue();
                if (sourceLang.trim().isEmpty() || targetLang.trim().isEmpty()) throw new IllegalArgumentException("Source or target language is empty! Needs to be a valid language!");

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
                            geminiAssSubtitleTranslator.translate(assSubtitle, sourceLang, targetLang, process);
                            subtitle.translationFinishStatus = AbstractSubtitle.TranslationFinishStatus.FINISHED;
                        }
                        //Write translated subtitle file if FINISHED
                        if (subtitle.translationFinishStatus == AbstractSubtitle.TranslationFinishStatus.FINISHED) {
                            Objects.requireNonNull(subtitle.sourceFile, "Source file of subtitle was NULL!");
                            String fileExtension = Files.getFileExtension(subtitle.sourceFile.getPath()).replace(".", "");
                            String fileName = Files.getNameWithoutExtension(subtitle.sourceFile.getPath());
                            String outFileSuffix = Main.getOptions().outputFileSuffix.getValue();
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
                process.exception = ex;
            }

        }, "Backend Translation Process Thread").start();

        return process;

    }

}
