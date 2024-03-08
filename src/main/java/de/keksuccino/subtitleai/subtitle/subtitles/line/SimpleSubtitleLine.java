package de.keksuccino.subtitleai.subtitle.subtitles.line;

import org.jetbrains.annotations.NotNull;

public class SimpleSubtitleLine extends AbstractSubtitleLine {

    public SimpleSubtitleLine(@NotNull String line) {
        this.rawLine = line;
    }

    @Override
    public @NotNull String serialize() {
        return this.getRawLine();
    }

}
