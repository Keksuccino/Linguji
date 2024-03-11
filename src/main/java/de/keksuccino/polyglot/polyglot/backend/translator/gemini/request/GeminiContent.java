package de.keksuccino.polyglot.polyglot.backend.translator.gemini.request;

import org.jetbrains.annotations.NotNull;
import java.util.HashMap;
import java.util.Map;

public class GeminiContent {

    public String role = "user";
    public final Map<String, String> parts = new HashMap<>();

    public static GeminiContent create() {
        return new GeminiContent();
    }

    public GeminiContent setRole(@NotNull String role) {
        this.role = role;
        return this;
    }

    public GeminiContent addPart(@NotNull String key, @NotNull String content) {
        this.parts.put(key, content);
        return this;
    }

}
