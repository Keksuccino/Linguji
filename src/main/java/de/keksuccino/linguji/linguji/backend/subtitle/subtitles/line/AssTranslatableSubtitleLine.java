package de.keksuccino.linguji.linguji.backend.subtitle.subtitles.line;

import de.keksuccino.linguji.linguji.backend.util.logger.LogHandler;
import de.keksuccino.linguji.linguji.backend.util.logger.SimpleLogger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.util.Objects;

public class AssTranslatableSubtitleLine extends AbstractTranslatableSubtitleLine {

    private static final SimpleLogger LOGGER = LogHandler.getLogger();

    public static final String ASS_LINE_BREAK = "\\N";
    public static final String ASS_SOFT_LINE_BREAK = "\\n";

    @NotNull
    protected String prefix = "";
    @NotNull
    protected String textFormatting = "";
    @NotNull
    protected String text = "";

    @Nullable
    public static AssTranslatableSubtitleLine create(@NotNull String line) {
        line = line.replace(ASS_SOFT_LINE_BREAK, " "); //remove "soft line breaks"
        line = line.replace(ASS_LINE_BREAK, LINE_BREAK_UNIVERSAL); //replace ASS line breaks with universal line breaks
        String[] parsed = parseDialogueLine(line);
        if (parsed != null) {
            AssTranslatableSubtitleLine assLine = new AssTranslatableSubtitleLine();
            assLine.rawLine = line;
            assLine.prefix = parsed[0];
            assLine.textFormatting = parsed[1];
            assLine.text = parsed[2];
            return assLine;
        }
        return null;
    }

    protected AssTranslatableSubtitleLine() {
    }

    @Override
    public @NotNull String getTextWithoutFormattingCodes() {
        return this.text;
    }

    @Override
    public @NotNull String serialize() {
        String t = (this.translatedText != null) ? this.translatedText : this.text;
        if (t == null) t = "";
        return (this.prefix + this.textFormatting + t.replace(LINE_BREAK_UNIVERSAL, ASS_LINE_BREAK));
    }

    /**
     * Splits the line in prefix [0], text formatting [1] and text [2].
     */
    @Nullable
    public static String[] parseDialogueLine(@NotNull String dialogueLine) {
        Objects.requireNonNull(dialogueLine);
        int charIndex = 0;
        int commaCounter = 0;
        String prefix = null;
        String text = "";
        String textFormatting = "";
        for (char c : dialogueLine.toCharArray()) {
            if (c == ',') commaCounter++;
            if (commaCounter == 9) {
                prefix = dialogueLine.substring(0, charIndex+1); //the "," is included at the end
                if (dialogueLine.length() >= charIndex+1) {
                    text = dialogueLine.substring(charIndex+1).trim(); //the "+1" is to skip the ","
                }
                break;
            }
            charIndex++;
        }
        if (prefix != null) {
            if (text.startsWith("{")) {
                int textCharIndex = 0;
                int depth = 0;
                for (char c : text.toCharArray()) {
                    if (c == '{') depth++;
                    if (c == '}') depth--;
                    if (depth == 0) {
//                        LOGGER.info("############### text: " + text + " | textCharIndex: " + textCharIndex);
                        textFormatting = text.substring(0, textCharIndex+1).trim();
                        text = text.substring(textCharIndex+1).trim();
                        break;
                    }
                    textCharIndex++;
                }
            }
            return new String[]{prefix, textFormatting, text};
        }
        return null;
    }

}
