package de.keksuccino.linguji.linguji.backend.engine.engines.gemini.safety;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class GeminiSafetySetting {

    public final String category;
    public final String threshold;

    public GeminiSafetySetting(@NotNull SafetyCategory category, @NotNull SafetyThreshold threshold) {
        this.category = category.name;
        this.threshold = threshold.name;
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

        @Nullable
        public static SafetyCategory getByName(@NotNull String name) {
            for (SafetyCategory category : SafetyCategory.values()) {
                if (category.name.equals(name)) return category;
            }
            return null;
        }

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

        @Nullable
        public static SafetyThreshold getByName(@NotNull String name) {
            for (SafetyThreshold threshold : SafetyThreshold.values()) {
                if (threshold.name.equals(name)) return threshold;
            }
            return null;
        }

    }

}
