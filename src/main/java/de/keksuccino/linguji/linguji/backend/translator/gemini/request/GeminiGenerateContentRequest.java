package de.keksuccino.linguji.linguji.backend.translator.gemini.request;

import de.keksuccino.linguji.linguji.backend.translator.gemini.safety.GeminiSafetySetting;
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
