package de.keksuccino.linguji.linguji.backend.subtitle.subtitles.line;

import de.keksuccino.linguji.linguji.backend.lib.logger.LogHandler;
import de.keksuccino.linguji.linguji.backend.lib.logger.SimpleLogger;
import org.jetbrains.annotations.NotNull;
import java.util.Objects;

public class SrtTranslatableSubtitleLine extends AbstractTranslatableSubtitleLine {

    private static final SimpleLogger LOGGER = LogHandler.getLogger();

    public static final String LINE_BREAK = "\n";
    public static final String LINE_BREAK_R = "\r";

    @NotNull
    protected String text = "";
    @NotNull
    protected String time = "";
    protected int lineNumber = -1;

    @NotNull
    public static SrtTranslatableSubtitleLine create(@NotNull String content, @NotNull String time, int lineNumber) throws Exception {

        Objects.requireNonNull(content);

        content = content.replace(LINE_BREAK, LINE_BREAK_UNIVERSAL); //replace line breaks with universal line breaks
        content = content.replace(LINE_BREAK_R, LINE_BREAK_UNIVERSAL); //replace line breaks with universal line breaks

        SrtTranslatableSubtitleLine srtLine = new SrtTranslatableSubtitleLine();
        srtLine.rawLine = content;
        srtLine.text = content;
        srtLine.time = Objects.requireNonNull(time);
        srtLine.lineNumber = lineNumber;

        if (lineNumber <= 0) throw new Exception("Illegal SRT subtitle line number: " + lineNumber);

        return srtLine;

    }

    protected SrtTranslatableSubtitleLine() {
    }

    @Override
    public @NotNull String getTextWithoutFormattingCodes() {
        return this.text;
    }

    @Override
    public @NotNull String serialize() {
        String t = (this.translatedText != null) ? this.translatedText : this.text;
        if (t == null) t = "";
        return this.lineNumber + "\n" + this.time + "\n" + t.replace(LINE_BREAK_UNIVERSAL, LINE_BREAK);
    }

}
