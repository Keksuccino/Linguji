package de.keksuccino.polyglot.polyglot.backend.translator.gemini.response;

import de.keksuccino.polyglot.polyglot.backend.translator.gemini.safety.GeminiSafetyRating;

public class GeminiPromptFeedback {

    public String blockReason;
    public GeminiSafetyRating[] safetyRatings;

}
