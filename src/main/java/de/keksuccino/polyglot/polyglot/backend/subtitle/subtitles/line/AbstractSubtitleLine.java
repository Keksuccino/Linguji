package de.keksuccino.polyglot.polyglot.backend.subtitle.subtitles.line;

import org.jetbrains.annotations.NotNull;

public abstract class AbstractSubtitleLine {

    @NotNull
    protected String rawLine = "";

    public void setRawLine(@NotNull String rawLine) {
        this.rawLine = rawLine;
    }

    @NotNull
    public String getRawLine() {
        return this.rawLine;
    }

    @NotNull
    public abstract String serialize();

}
