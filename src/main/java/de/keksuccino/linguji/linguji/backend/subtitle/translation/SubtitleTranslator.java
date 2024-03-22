package de.keksuccino.linguji.linguji.backend.subtitle.translation;

import de.keksuccino.linguji.linguji.backend.Backend;
import de.keksuccino.linguji.linguji.backend.subtitle.subtitles.AbstractSubtitle;
import de.keksuccino.linguji.linguji.backend.subtitle.subtitles.line.AbstractSubtitleLine;
import de.keksuccino.linguji.linguji.backend.subtitle.subtitles.line.AbstractTranslatableSubtitleLine;
import de.keksuccino.linguji.linguji.backend.translator.AbstractTranslationEngine;
import de.keksuccino.linguji.linguji.backend.translator.FallbackTranslatorBehaviour;
import de.keksuccino.linguji.linguji.backend.translator.SharedTranslatorOptions;
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
    protected final AbstractTranslationEngine primaryTranslator;
    @NotNull
    public AbstractTranslationEngine fallbackTranslator;
    protected FallbackTranslatorBehaviour fallbackTranslatorBehaviour = Objects.requireNonNull(SharedTranslatorOptions.getFallbackTranslatorBehaviour());

    public SubtitleTranslator(@NotNull AbstractTranslationEngine primaryTranslator, @NotNull AbstractTranslationEngine fallbackTranslator) {
        this.primaryTranslator = Objects.requireNonNull(primaryTranslator);
        this.fallbackTranslator = Objects.requireNonNull(fallbackTranslator);
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
            this.translatePacket(packet, 0, false, process);
        }

    }

    protected void translatePacket(@NotNull List<IndexedLine> packet, int failedTries, boolean fallbackFullPacket, @NotNull TranslationProcess process) throws Exception {

        if (!process.running) return;

        //Translate problematic lines
        if (fallbackTranslatorBehaviour == FallbackTranslatorBehaviour.TRY_TRANSLATE_BAD_PARTS_OF_PACKET) {
            for (IndexedLine line : packet) {
                if (!process.running) return;
                if (line.line.problematic && (line.line.getTranslatedText() == null)) {
                    line.line.setTranslatedText(Objects.requireNonNull(this.fallbackTranslator.translate(line.line.getTextWithoutFormattingCodes(), process)));
                }
            }
        }

        if (!process.running) return;

        String normalLinesString = this.buildNormalLinesString(packet, process);

        //Translate normal lines
        Exception translateException = null;
        String translatedNormal = null;
        try {
            translatedNormal = fallbackFullPacket ? this.fallbackTranslator.translate(normalLinesString, process) : this.primaryTranslator.translate(normalLinesString, process);
        } catch (Exception ex) {
            if (fallbackFullPacket) throw ex;
            translateException = ex;
        }

        if (!process.running) return;

        //Check if fallback translator should get used
        if (!fallbackFullPacket && (translateException != null) || (translatedNormal == null)) {
            if (fallbackTranslatorBehaviour != FallbackTranslatorBehaviour.DONT_USE_FALLBACK) {
                if (translateException != null) {
                    LOGGER.warn("Primary translator failed with an error! Trying to translate with fallback translator..", translateException);
                } else {
                    LOGGER.warn("Primary translator returned NULL as translation! Trying to translate with fallback translator..");
                }
                if (!process.running) return;
                if (fallbackTranslatorBehaviour == FallbackTranslatorBehaviour.TRY_TRANSLATE_BAD_PARTS_OF_PACKET) {
                    this.markProblematicLines(packet, process);
                    //If no problematic lines were found when translating lines one by one, use fallback for whole packet
                    if (!this.containsProblematicLines(packet, process)) {
                        LOGGER.warn("No problematic/bad lines where found when checking lines one by one! Will use the fallback translator for the full packet!");
                        if (!process.running) return;
                        this.translatePacket(packet, failedTries, true, process);
                    } else {
                        if (!process.running) return;
                        this.translatePacket(packet, failedTries, false, process);
                    }
                    return;
                } else if (fallbackTranslatorBehaviour == FallbackTranslatorBehaviour.TRANSLATE_FULL_PACKET) {
                    if (!process.running) return;
                    this.translatePacket(packet, failedTries, true, process);
                    return;
                }
            }
        }

        if (translateException != null) throw translateException;
        if (translatedNormal == null) return;

        String[] translatedNormalLines = translatedNormal.split("\n");

        if (!process.running) return;

        if (translatedNormalLines.length == packet.size()) {
            int translatedIndex = 0;
            for (String line : translatedNormalLines) {
                if (!process.running) return;
                AbstractTranslatableSubtitleLine line2 = packet.get(translatedIndex).line;
                if (!line2.problematic) line2.setTranslatedText(line);
                if (line2.problematic && (line2.getTranslatedText() == null)) throw new NullPointerException("No translation found for problematic line!");
                process.currentSubtitleFinishedLines.add(line2);
                translatedIndex++;
            }
        } else {
            failedTries++;
            if (!process.running) return;
            if (failedTries <= Backend.getOptions().triesBeforeErrorInvalidLineCount.getValue()) {
                LOGGER.warn("TranslationEngine returned invalid amount of translated lines! Trying again..");
                ThreadUtils.sleep(Backend.getOptions().waitMillisBeforeNextTry.getValue());
                this.translatePacket(packet, failedTries, fallbackFullPacket, process);
                return;
            }
            throw new IllegalStateException("TranslationEngine returned invalid amount of lines!");
        }

    }

    protected void markProblematicLines(@NotNull List<IndexedLine> packet, @NotNull TranslationProcess process) {
        LOGGER.info("----------- Searching for problematic lines..");
        for (IndexedLine line : packet) {
            if (!process.running) return;
            try {
                Objects.requireNonNull(this.primaryTranslator.translate(line.line.getTextWithoutFormattingCodes(), process));
            } catch (Exception ignore) {
                LOGGER.warn("Problematic line found! Will try to use fallback translator for translation: " + line.line.getTextWithoutFormattingCodes());
                line.line.problematic = true;
            }
        }
        LOGGER.info("----------- Finished searching for problematic lines!");
    }

    protected boolean containsProblematicLines(@NotNull List<IndexedLine> packet, @NotNull TranslationProcess process) {
        for (IndexedLine line : packet) {
            if (!process.running) return false;
            if (line.line.problematic) return true;
        }
        return false;
    }

    @NotNull
    protected String buildNormalLinesString(@NotNull List<IndexedLine> packet, @NotNull TranslationProcess process) {
        StringBuilder linesString = new StringBuilder();
        boolean firstLine = true;
        for (IndexedLine line : packet) {
            if (!process.running) return "";
            if (!firstLine) linesString.append("\n");
            if (!line.line.problematic) {
                linesString.append(line.line.getTextWithoutFormattingCodes());
            } else {
                linesString.append("<removed_line_placeholder>");
            }
            firstLine = false;
        }
        return linesString.toString();
    }

    protected record IndexedLine(@NotNull AbstractTranslatableSubtitleLine line, int index) {
    }

}
