package de.keksuccino.subtitleai.translator.gemini.response;

import de.keksuccino.subtitleai.translator.gemini.safety.GeminiSafetyRating;

public class GeminiPromptFeedback {

    public String blockReason;
    public GeminiSafetyRating[] safetyRatings;

}
