package de.keksuccino.linguji.linguji.backend.engine.engines.gemini.response;

import de.keksuccino.linguji.linguji.backend.engine.engines.gemini.safety.GeminiSafetyRating;

public class GeminiPromptFeedback {

    public String blockReason;
    public GeminiSafetyRating[] safetyRatings;

}
