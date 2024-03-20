package de.keksuccino.linguji.linguji.backend.subtitle.translation;

import de.keksuccino.linguji.linguji.backend.Backend;
import de.keksuccino.linguji.linguji.backend.subtitle.subtitles.AbstractSubtitle;
import de.keksuccino.linguji.linguji.backend.subtitle.subtitles.line.AbstractSubtitleLine;
import de.keksuccino.linguji.linguji.backend.subtitle.subtitles.line.AbstractTranslatableSubtitleLine;
import de.keksuccino.linguji.linguji.backend.translator.AbstractTranslationEngine;
import de.keksuccino.linguji.linguji.backend.translator.libretranslate.LibreTranslationEngine;
import de.keksuccino.linguji.linguji.backend.util.ThreadUtils;
import de.keksuccino.linguji.linguji.backend.util.logger.LogHandler;
import de.keksuccino.linguji.linguji.backend.util.logger.SimpleLogger;
import org.jetbrains.annotations.NotNull;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class SubtitleTranslator<T extends AbstractSubtitle> {

    private static final SimpleLogger LOGGER = LogHandler.getLogger();

    @NotNull
    protected final AbstractTranslationEngine translator;
    protected final boolean threaded;
    @NotNull
    public AbstractTranslationEngine fallbackTranslator;

    public SubtitleTranslator(@NotNull AbstractTranslationEngine translator, boolean threaded) {
        this.translator = Objects.requireNonNull(translator);
        this.fallbackTranslator = new LibreTranslationEngine(Backend.getOptions().libreTranslateUrl.getValue(), Backend.getOptions().libreTranslateApiKey.getValue(), this.translator.sourceLanguage, this.translator.targetLanguage);
        this.threaded = threaded;
        if (threaded) throw new RuntimeException("Threading is not fully implemented yet. It works, but is not limited to a max number of threads, so it basically just request-bombs the API and kills it.");
    }

    public void translate(@NotNull T subtitle, @NotNull TranslationProcess process) throws Exception {

        List<List<IndexedLine>> linePackets = new ArrayList<>();
        List<IndexedLine> currentPacket = new ArrayList<>();
        int totalTranslatableLineCount = 0;
        int index = 0;
        int lineCount = 0;
        for (AbstractSubtitleLine line : subtitle.getLines()) {
            if (!process.running) return;
            if (line instanceof AbstractTranslatableSubtitleLine t) {
                //Add translatable lines, but skip empty ones to not confuse the AI too much...
                if (!t.getTextWithoutFormattingCodes().replace(AbstractTranslatableSubtitleLine.LINE_BREAK_UNIVERSAL, "").trim().isEmpty()) {
                    currentPacket.add(new IndexedLine(t, index));
                    lineCount++;
                    totalTranslatableLineCount++;
                }
            }
            //Start new packet and reset counter if max lines per packet reached
            if (lineCount >= Backend.getOptions().linesPerPacket.getValue()) {
                linePackets.add(currentPacket);
                currentPacket = new ArrayList<>();
                lineCount = 0;
            }
            index++;
        }
        //Manually add last packet to list if it didn't get add automatically (because LINES_PER_PACKET limit not reached)
        if (!currentPacket.isEmpty()) {
            linePackets.add(currentPacket);
        }
        //Add last line packet to previous one if last one only has one line
        if (linePackets.size() >= 2) {
            if (linePackets.get(linePackets.size()-1).size() <= 1) {
                linePackets.get(linePackets.size()-2).addAll(linePackets.get(linePackets.size()-1));
                linePackets.remove(linePackets.size()-1);
            }
        }

        if (!process.running) return;

        process.currentSubtitleFinishedLines.clear();
        process.currentSubtitleTranslatableLinesCount = totalTranslatableLineCount;

        if (this.threaded) {

            List<TranslationThreadFeedback> threadFeedbacks = new ArrayList<>();

            for (List<IndexedLine> packet : linePackets) {

                StringBuilder linesString = new StringBuilder();
                boolean firstLine = true;
                for (IndexedLine line : packet) {
                    if (!firstLine) linesString.append("\n");
                    linesString.append(line.line.getTextWithoutFormattingCodes());
                    firstLine = false;
                }

                TranslationThreadFeedback feedback = new TranslationThreadFeedback();
                threadFeedbacks.add(feedback);
                Thread t = new Thread(() -> {
                    try {
                        this.translatePacket(packet, linesString.toString(), 0, false, feedback, process);
                        feedback.completed = true;
                    } catch (Exception ex) {
                        feedback.exception = ex;
                    }
                }, "SubtitleTranslator Thread");
                t.setDaemon(true);
                t.start();

            }

            //Wait for all translation threads
            while (true) {
                boolean completed = true;
                Exception ex = null;
                for (TranslationThreadFeedback feedback : threadFeedbacks) {
                    if (feedback.exception != null) {
                        completed = false;
                        ex = feedback.exception;
                        break;
                    }
                    if (!feedback.completed) {
                        completed = false;
                        break;
                    }
                }
                if (ex != null) {
                    threadFeedbacks.forEach(feedback -> feedback.stopped = true);
                    throw ex;
                }
                if (completed) break;
                ThreadUtils.sleep(100);
            }

        } else {

            TranslationThreadFeedback dummyFeedback = new TranslationThreadFeedback();

            for (List<IndexedLine> packet : linePackets) {

                if (!process.running) return;

                StringBuilder linesString = new StringBuilder();
                boolean firstLine = true;
                for (IndexedLine line : packet) {
                    if (!process.running) return;
                    if (!firstLine) linesString.append("\n");
                    linesString.append(line.line.getTextWithoutFormattingCodes());
                    firstLine = false;
                }

                this.translatePacket(packet, linesString.toString(), 0, false, dummyFeedback, process);

            }

        }

    }

    protected void translatePacket(@NotNull List<IndexedLine> packet, @NotNull String linesString, int failedTries, boolean fallback, @NotNull TranslationThreadFeedback threadFeedback, @NotNull TranslationProcess process) throws Exception {

        if (threadFeedback.stopped) return;
        if (!process.running) return;

        Exception translateException = null;
        String translated = null;
        try {
            translated = fallback ? this.fallbackTranslator.translate(linesString, process) : this.translator.translate(linesString, process);
        } catch (Exception ex) {
            if (Backend.getOptions().useFallbackTranslator.getValue() && !fallback) {
                translateException = ex;
            } else {
                throw ex;
            }
        }

        if (threadFeedback.stopped) return;
        if (!process.running) return;

        //Handle fallback translator
        if ((translateException != null) || (translated == null)) {
            if (Backend.getOptions().useFallbackTranslator.getValue()) {
                if (translateException != null) {
                    LOGGER.warn("Translation of line packet failed with an error! Trying to translate with fallback translator..", translateException);
                } else {
                    LOGGER.warn("Main translator returned NULL as translation! Trying to translate with fallback translator..");
                }
                fallback = true;
                translated = this.fallbackTranslator.translate(linesString, process);
            }
        }

        if (translated == null) return;
        String[] translatedLines = translated.split("\n");

        if (threadFeedback.stopped) return;
        if (!process.running) return;

        if (translatedLines.length == packet.size()) {
            int translatedIndex = 0;
            for (String line : translatedLines) {
                if (threadFeedback.stopped) return;
                if (!process.running) return;
                AbstractTranslatableSubtitleLine line2 = packet.get(translatedIndex).line;
                line2.setTranslatedText(line);
                process.currentSubtitleFinishedLines.add(line2);
                translatedIndex++;
            }
        } else {
            failedTries++;
            if (!process.running) return;
            if (threadFeedback.stopped) return;
            if (failedTries <= Backend.getOptions().triesBeforeErrorInvalidLineCount.getValue()) {
                LOGGER.warn("TranslationEngine returned invalid amount of translated lines! Trying again..");
                ThreadUtils.sleep(Backend.getOptions().waitMillisBeforeNextTry.getValue());
                this.translatePacket(packet, linesString, failedTries, fallback, threadFeedback, process);
                return;
            }
            throw new IllegalStateException("TranslationEngine returned invalid amount of lines!");
        }

    }

    protected record IndexedLine(@NotNull AbstractTranslatableSubtitleLine line, int index) {
    }

    protected static class TranslationThreadFeedback {

        protected volatile boolean stopped = false;
        protected volatile boolean completed = false;
        protected volatile Exception exception = null;

    }

}
