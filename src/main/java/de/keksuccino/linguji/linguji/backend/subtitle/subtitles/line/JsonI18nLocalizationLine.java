package de.keksuccino.linguji.linguji.backend.subtitle.subtitles.line;

import org.jetbrains.annotations.NotNull;

public class JsonI18nLocalizationLine extends AbstractTranslatableSubtitleLine {

    public static final String LINE_BREAK = "\n";

    @NotNull
    protected String key = "";
    @NotNull
    protected String value = "";

    @NotNull
    public static JsonI18nLocalizationLine create(@NotNull String key, @NotNull String value) {
        JsonI18nLocalizationLine entry = new JsonI18nLocalizationLine();
        entry.key = key;
        entry.value = value.replace(LINE_BREAK, LINE_BREAK_UNIVERSAL);
        entry.rawLine = "\"" + key + "\": \"" + value + "\"";
        return entry;
    }

    protected JsonI18nLocalizationLine() {}

    @Override
    public @NotNull String getTextWithoutFormattingCodes() {
        return this.value;
    }

    @Override
    public @NotNull String serialize() {
        String t = (this.translatedText != null) ? this.translatedText : this.value;
        if (t == null) t = "";
        return "\"" + this.key + "\": \"" + t.replace(LINE_BREAK_UNIVERSAL, LINE_BREAK) + "\"";
    }

    @NotNull
    public String getKey() {
        return this.key;
    }

}