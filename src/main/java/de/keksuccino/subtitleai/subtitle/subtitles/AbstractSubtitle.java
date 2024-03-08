package de.keksuccino.subtitleai.subtitle.subtitles;

import de.keksuccino.subtitleai.subtitle.subtitles.line.AbstractSubtitleLine;
import org.jetbrains.annotations.NotNull;
import java.util.ArrayList;
import java.util.List;

public abstract class AbstractSubtitle {

    @NotNull
    protected List<AbstractSubtitleLine> lines = new ArrayList<>();

    @NotNull
    public abstract String getFileExtension();

    @NotNull
    public List<AbstractSubtitleLine> getLines() {
        return this.lines;
    }

    @NotNull
    public abstract String serialize();

}
