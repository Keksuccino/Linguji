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
    @NotNull
    public AbstractTranslationEngine fallbackTranslator;

    public SubtitleTranslator(@NotNull AbstractTranslationEngine translator) {
        this.translator = Objects.requireNonNull(translator);
        this.fallbackTranslator = new LibreTranslationEngine(Backend.getOptions().libreTranslateUrl.getValue(), Backend.getOptions().libreTranslateApiKey.getValue(), this.translator.sourceLanguage, this.translator.targetLanguage);
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

        for (List<IndexedLine> packet : linePackets) {

            if (!process.running) return;

            this.translatePacket(packet, this.buildLinesString(packet, process), 0, false, false, process);

        }

    }

    protected void translatePacket(@NotNull List<IndexedLine> packet, @NotNull String linesString, int failedTries, boolean fallback, boolean forceMain, @NotNull TranslationProcess process) throws Exception {

        if (!process.running) return;

        Exception translateException = null;
        String translated = null;
        try {
            translated = (fallback && !forceMain) ? this.fallbackTranslator.translate(linesString, process) : this.translator.translate(linesString, process);
        } catch (Exception ex) {
            if (!forceMain && Backend.getOptions().useFallbackTranslator.getValue() && !fallback) {
                translateException = ex;
            } else {
                throw ex;
            }
        }

        if (!process.running) return;

        if (forceMain && (translated == null)) return;

        //Handle fallback translator
        if ((translateException != null) || (translated == null)) {
            if (Backend.getOptions().useFallbackTranslator.getValue()) {
                if (translateException != null) {
                    LOGGER.warn("Translation of line packet failed with an error! Trying to translate with combination of main and fallback translator..", translateException);
                } else {
                    LOGGER.warn("Main translator returned NULL as translation! Trying to translate with combination of main and fallback translator..");
                }
                fallback = true;
                if (Backend.getOptions().fallbackLinePacketSize.getValue() > Backend.getOptions().linesPerPacket.getValue()) {
                    Backend.getOptions().fallbackLinePacketSize.setValue(Backend.getOptions().linesPerPacket.getValue());
                }
                if (packet.size() <= Backend.getOptions().fallbackLinePacketSize.getValue()) {
                    this.translatePacket(packet, linesString, failedTries, fallback, forceMain, process);
                } else {
                    //Split packet into small 2-line packets for the fallback translator
                    List<List<IndexedLine>> splitPackets = new ArrayList<>();
                    splitPackets.add(new ArrayList<>());
                    for (IndexedLine line : packet) {
                        if (!process.running) return;
                        List<IndexedLine> current = splitPackets.get(splitPackets.size()-1);
                        if (current.size() >= Backend.getOptions().fallbackLinePacketSize.getValue()) {
                            current = new ArrayList<>();
                            splitPackets.add(current);
                        }
                        current.add(line);
                    }
                    for (List<IndexedLine> splitPacket : splitPackets) {
                        if (!process.running) return;
                        String splitLinesString = this.buildLinesString(splitPacket, process);
                        boolean failed = false;
                        boolean packContainsUntranslatedLines = false;
                        try {
                            this.translatePacket(splitPacket, splitLinesString, failedTries, false, true, process);
                        } catch (Exception ex) {
                            LOGGER.warn("Fallback Logic: Main translator failed to translate split-packet! Trying fallback translator now..");
                            failed = true;
                        }
                        if (!process.running) return;
                        if (!failed) {
                            packContainsUntranslatedLines = this.containsUntranslatedLines(splitPacket, process);
                            if (packContainsUntranslatedLines) {
                                LOGGER.warn("Fallback Logic: Main translator failed to translate split-packet! Trying fallback translator now..");
                            }
                        }
                        if (failed || packContainsUntranslatedLines) {
                            this.translatePacket(splitPacket, splitLinesString, failedTries, fallback, false, process);
                        }
                    }
                }
                return;
            }
        }

        if (translated == null) return;
        String[] translatedLines = translated.split("\n");

        if (!process.running) return;

        if (translatedLines.length == packet.size()) {
            int translatedIndex = 0;
            for (String line : translatedLines) {
                if (!process.running) return;
                AbstractTranslatableSubtitleLine line2 = packet.get(translatedIndex).line;
                line2.setTranslatedText(line);
                process.currentSubtitleFinishedLines.add(line2);
                translatedIndex++;
            }
        } else {
            failedTries++;
            if (!process.running) return;
            if (failedTries <= Backend.getOptions().triesBeforeErrorInvalidLineCount.getValue()) {
                LOGGER.warn("TranslationEngine returned invalid amount of translated lines! Trying again..");
                ThreadUtils.sleep(Backend.getOptions().waitMillisBeforeNextTry.getValue());
                this.translatePacket(packet, linesString, failedTries, fallback, forceMain, process);
                return;
            }
            throw new IllegalStateException("TranslationEngine returned invalid amount of lines!");
        }

    }

    protected void markProblematicLines(@NotNull List<IndexedLine> packet, @NotNull TranslationProcess process) {



    }

    protected boolean containsUntranslatedLines(@NotNull List<IndexedLine> packet, @NotNull TranslationProcess process) {
        for (IndexedLine line : packet) {
            if (!process.running) return false;
            if (line.line.getTranslatedText() == null) {
                return true;
            }
        }
        return false;
    }

    @NotNull
    protected String buildLinesString(@NotNull List<IndexedLine> packet, @NotNull TranslationProcess process) {
        StringBuilder linesString = new StringBuilder();
        boolean firstLine = true;
        for (IndexedLine line : packet) {
            if (!process.running) return "";
            if (!firstLine) linesString.append("\n");
            linesString.append(line.line.getTextWithoutFormattingCodes());
            firstLine = false;
        }
        return linesString.toString();
    }

    protected record IndexedLine(@NotNull AbstractTranslatableSubtitleLine line, int index) {
    }

}
