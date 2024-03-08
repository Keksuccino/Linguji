package de.keksuccino.subtitleai.subtitle.translation;

import de.keksuccino.subtitleai.ai.AiTranslator;
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

    public static final int LINES_PER_PACKET = 10;
    public static final int MAX_TRIES_BEFORE_ERROR_INVALID_LINE_COUNT = 2000;
    public static final int WAIT_MS_AFTER_FAILED_TRY = 3000;

    @NotNull
    protected final AiTranslator translator;

    public SubtitleTranslator(@NotNull AiTranslator translator) {
        this.translator = Objects.requireNonNull(translator);
    }

    @NotNull
    public T translate(@NotNull T subtitle, @NotNull String sourceLanguage, @NotNull String targetLanguage) throws Exception {

        List<List<IndexedLine>> linePackets = new ArrayList<>();
        List<IndexedLine> currentPacket = new ArrayList<>();
        int index = 0;
        int lineCount = 0;
        for (AbstractSubtitleLine line : subtitle.getLines()) {
            if (line instanceof AbstractTranslatableSubtitleLine t) {
                //Add translatable lines, but skip empty ones to not confuse the AI too much...
                if (!t.getTextWithoutFormattingCodes().replace(AbstractTranslatableSubtitleLine.LINE_BREAK_UNIVERSAL, "").trim().isEmpty()) {
                    currentPacket.add(new IndexedLine(t, index));
                    lineCount++;
                }
            }
            //Start new packet and reset counter if max lines per packet reached
            if (lineCount >= LINES_PER_PACKET) {
                linePackets.add(currentPacket);
                currentPacket = new ArrayList<>();
                lineCount = 0;
            }
            index++;
        }
        //Add last line packet to previous one if last one only has one line
        if (linePackets.size() >= 2) {
            if (linePackets.get(linePackets.size()-1).size() <= 1) {
                linePackets.get(linePackets.size()-2).addAll(linePackets.get(linePackets.size()-1));
                linePackets.remove(linePackets.size()-1);
            }
        }

        for (List<IndexedLine> packet : linePackets) {

            StringBuilder linesString = new StringBuilder();
            boolean firstLine = true;
            for (IndexedLine line : packet) {
                if (!firstLine) linesString.append("\n");
                linesString.append(line.line.getTextWithoutFormattingCodes());
                firstLine = false;
            }

            this.translatePacket(packet, linesString.toString(), sourceLanguage, targetLanguage, 0);

        }

        return subtitle;

    }

    protected void translatePacket(@NotNull List<IndexedLine> packet, @NotNull String linesString, @NotNull String sourceLanguage, @NotNull String targetLanguage, int failedTries) throws Exception {

        String translated = this.translator.translate(linesString, sourceLanguage, targetLanguage);
        String[] translatedLines = translated.split("\n");

        if (translatedLines.length == packet.size()) {
            int translatedIndex = 0;
            for (String line : translatedLines) {
                packet.get(translatedIndex).line.setTranslatedText(line);
                translatedIndex++;
            }
        } else {
            failedTries++;
            if (failedTries <= MAX_TRIES_BEFORE_ERROR_INVALID_LINE_COUNT) {
                LOGGER.info("############### INVALID AMOUNT OF PACKET LINES RETURNED! TRYING AGAIN!");
                ThreadUtils.sleep(WAIT_MS_AFTER_FAILED_TRY);
                this.translatePacket(packet, linesString, sourceLanguage, targetLanguage, failedTries);
                return;
            }
            throw new IllegalStateException("AiTranslator returned invalid amount of lines!");
        }

    }

    protected record IndexedLine(@NotNull AbstractTranslatableSubtitleLine line, int index) {
    }

}
