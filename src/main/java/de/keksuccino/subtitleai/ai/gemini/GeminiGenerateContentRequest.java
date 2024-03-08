package de.keksuccino.subtitleai.ai.gemini;

import org.jetbrains.annotations.NotNull;

public class GeminiGenerateContentRequest {

    public GeminiContent[] contents;
    public GeminiSafetySetting[] safetySettings;

    @NotNull
    public static GeminiGenerateContentRequest create() {
        return new GeminiGenerateContentRequest();
    }

    public GeminiGenerateContentRequest setContents(@NotNull GeminiContent... contents) {
        this.contents = contents;
        return this;
    }

    public GeminiGenerateContentRequest setSafetySettings(@NotNull GeminiSafetySetting... settings) {
        this.safetySettings = settings;
        return this;
    }

}
