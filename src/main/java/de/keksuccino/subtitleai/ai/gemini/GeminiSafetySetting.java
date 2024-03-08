package de.keksuccino.subtitleai.ai.gemini;

import org.jetbrains.annotations.NotNull;

public class GeminiSafetySetting {

    public final String category;
    public final String threshold;

    public GeminiSafetySetting(@NotNull SafetyCategory category, @NotNull SafetyThreshold threshold) {
        this.category = category.name;
        this.threshold = threshold.name;
    }

    public enum SafetyThreshold {

        UNSPECIFIED("HARM_BLOCK_THRESHOLD_UNSPECIFIED"),
        BLOCK_LOW_AND_ABOVE("BLOCK_LOW_AND_ABOVE"),
        BLOCK_MEDIUM_AND_ABOVE("BLOCK_MEDIUM_AND_ABOVE"),
        BLOCK_ONLY_HIGH("BLOCK_ONLY_HIGH"),
        BLOCK_NONE("BLOCK_NONE");

        public final String name;

        SafetyThreshold(@NotNull String name) {
            this.name = name;
        }

    }

    public enum SafetyCategory {

        HARASSMENT("HARM_CATEGORY_HARASSMENT"),
        HATE_SPEECH("HARM_CATEGORY_HATE_SPEECH"),
        SEXUALLY_EXPLICIT("HARM_CATEGORY_SEXUALLY_EXPLICIT"),
        DANGEROUS_CONTENT("HARM_CATEGORY_DANGEROUS_CONTENT");

        public final String name;

        SafetyCategory(@NotNull String name) {
            this.name = name;
        }

    }

}
