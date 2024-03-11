package de.keksuccino.subtitleai.subtitle.translation;

import de.keksuccino.subtitleai.Main;
import de.keksuccino.subtitleai.translator.ITranslationEngine;
import de.keksuccino.subtitleai.subtitle.subtitles.AbstractSubtitle;
import de.keksuccino.subtitleai.subtitle.subtitles.line.AbstractSubtitleLine;
import de.keksuccino.subtitleai.subtitle.subtitles.line.AbstractTranslatableSubtitleLine;
import de.keksuccino.subtitleai.util.ThreadUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class SubtitleTranslator<T extends AbstractSubtitle> {

    private static final Logger LOGGER = LogManager.getLogger();

    @NotNull
    protected final ITranslationEngine translator;
    protected final boolean threaded;

    public SubtitleTranslator(@NotNull ITranslationEngine translator, boolean threaded) {
        this.translator = Objects.requireNonNull(translator);
        this.threaded = threaded;
        if (threaded) throw new RuntimeException("Threading is not fully implemented yet. It works, but is not limited to a max number of threads, so it basically just request-bombs the API and kills it.");
    }

    public void translate(@NotNull T subtitle, @NotNull String sourceLanguage, @NotNull String targetLanguage, @NotNull TranslationProcess process) throws Exception {

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
            if (lineCount >= Main.getOptions().linesPerPacket.getValue()) {
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
                new Thread(() -> {
                    try {
                        this.translatePacket(packet, linesString.toString(), sourceLanguage, targetLanguage, 0, feedback, process);
                        feedback.completed = true;
                    } catch (Exception ex) {
                        feedback.exception = ex;
                    }
                }, "SubtitleTranslator Translation Thread").start();

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

                this.translatePacket(packet, linesString.toString(), sourceLanguage, targetLanguage, 0, dummyFeedback, process);

            }

        }

    }

    protected void translatePacket(@NotNull List<IndexedLine> packet, @NotNull String linesString, @NotNull String sourceLanguage, @NotNull String targetLanguage, int failedTries, @NotNull TranslationThreadFeedback threadFeedback, @NotNull TranslationProcess process) throws Exception {

        String translated = this.translator.translate(linesString, sourceLanguage, targetLanguage);
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
            if (failedTries <= Main.getOptions().triesBeforeErrorInvalidLineCount.getValue()) {
                LOGGER.info("AiTranslator returned invalid amount of translated lines! Trying again..");
                ThreadUtils.sleep(Main.getOptions().waitMillisBeforeNextTry.getValue());
                this.translatePacket(packet, linesString, sourceLanguage, targetLanguage, failedTries, threadFeedback, process);
                return;
            }
            throw new IllegalStateException("AiTranslator returned invalid amount of lines!");
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
