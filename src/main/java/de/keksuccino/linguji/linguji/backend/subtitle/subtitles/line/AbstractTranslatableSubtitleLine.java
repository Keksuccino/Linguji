package de.keksuccino.linguji.linguji.backend.subtitle.subtitles.line;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class AbstractTranslatableSubtitleLine extends AbstractSubtitleLine {

    public static final String LINE_BREAK_UNIVERSAL = "<br>";

    @Nullable
    protected volatile String translatedText = null;
    public volatile boolean problematic = false;

    /**
     * Returns the text without formatting codes. Line breaks ({@code <br>}) don't get removed.
     */
    @NotNull
    public abstract String getTextWithoutFormattingCodes();

    public void setTranslatedText(@Nullable String translatedText) {
        this.translatedText = translatedText;
    }

    @Nullable
    public String getTranslatedText() {
        return this.translatedText;
    }

}
