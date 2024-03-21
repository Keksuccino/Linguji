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
            this.translatePacket(packet, 0, process);
        }

    }

    protected void translatePacket(@NotNull List<IndexedLine> packet, int failedTries, @NotNull TranslationProcess process) throws Exception {

        if (!process.running) return;

        //Translate problematic lines
        for (IndexedLine line : packet) {
            if (!process.running) return;
            if (line.line.problematic && (line.line.getTranslatedText() == null)) {
                line.line.setTranslatedText(Objects.requireNonNull(this.fallbackTranslator.translate(line.line.getTextWithoutFormattingCodes(), process)));
            }
        }

        if (!process.running) return;

        String normalLinesString = this.buildNormalLinesString(packet, process);

        //Translate normal lines
        Exception translateException = null;
        String translatedNormal = null;
        try {
            translatedNormal = this.translator.translate(normalLinesString, process);
        } catch (Exception ex) {
            translateException = ex;
        }

        if (!process.running) return;

        //Check if fallback translator should get used
        if ((translateException != null) || (translatedNormal == null)) {
            if (Backend.getOptions().useFallbackTranslator.getValue()) {
                if (translateException != null) {
                    LOGGER.warn("Translation of line packet failed with an error! Trying to translate with combination of main and fallback translator..", translateException);
                } else {
                    LOGGER.warn("Main translator returned NULL as translation! Trying to translate with combination of main and fallback translator..");
                }
                if (!process.running) return;
                this.markProblematicLines(packet, process);
                //If no problematic lines were found when translating lines one by one, use the problematic line check responses as translation (because still better than Libre...)
                if (!this.containsProblematicLines(packet, process) && this.allLinesHaveProblematicLineCheckResponse(packet, process)) {
                    LOGGER.warn("No problematic lines where found when checking lines one by one! Will use the one-by-one translation because it is still better than using LibreTranslate.");
                    StringBuilder normalBuilder = new StringBuilder();
                    boolean firstLine = true;
                    for (IndexedLine line : packet) {
                        if (!process.running) return;
                        if (!firstLine) normalBuilder.append("\n");
                        normalBuilder.append(Objects.requireNonNullElse(line.line.problematicLineCheckResponse, line.line.getTextWithoutFormattingCodes()));
                        firstLine = false;
                    }
                    translatedNormal = normalBuilder.toString();
                    translateException = null;
                } else {
                    this.translatePacket(packet, failedTries, process);
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
                this.translatePacket(packet, failedTries, process);
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
                line.line.problematicLineCheckResponse = Objects.requireNonNull(this.translator.translate(line.line.getTextWithoutFormattingCodes(), process));
            } catch (Exception ignore) {
                LOGGER.warn("Problematic line found! Will try to use fallback translator for translation: " + line.line.getTextWithoutFormattingCodes());
                line.line.problematic = true;
            }
        }
        LOGGER.info("----------- Finished searching for problematic lines!");
    }

    protected boolean allLinesHaveProblematicLineCheckResponse(@NotNull List<IndexedLine> packet, @NotNull TranslationProcess process) {
        for (IndexedLine line : packet) {
            if (!process.running) return false;
            if (line.line.problematicLineCheckResponse == null) return false;
        }
        return true;
    }

    protected boolean containsProblematicLines(@NotNull List<IndexedLine> packet, @NotNull TranslationProcess process) {
        for (IndexedLine line : packet) {
            if (!process.running) return false;
            if (line.line.problematic) return true;
        }
        return false;
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
