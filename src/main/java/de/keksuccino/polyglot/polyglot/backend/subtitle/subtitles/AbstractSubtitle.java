package de.keksuccino.polyglot.polyglot.backend.subtitle.subtitles;

import de.keksuccino.polyglot.polyglot.backend.subtitle.subtitles.line.AbstractSubtitleLine;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public abstract class AbstractSubtitle {

    @NotNull
    public TranslationFinishStatus translationFinishStatus = TranslationFinishStatus.NOT_FINISHED;
    @Nullable
    public File sourceFile = null;

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

    public enum TranslationFinishStatus {
        NOT_FINISHED,
        FINISHED,
        FINISHED_WITH_EXCEPTIONS
    }

}
