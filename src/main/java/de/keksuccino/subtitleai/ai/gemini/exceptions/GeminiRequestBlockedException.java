package de.keksuccino.subtitleai.ai.gemini.exceptions;

import de.keksuccino.subtitleai.ai.exceptions.ProfanityException;
import org.jetbrains.annotations.NotNull;

public class GeminiRequestBlockedException extends ProfanityException {

    @NotNull
    public final String reason;

    public GeminiRequestBlockedException(@NotNull String reason, @NotNull String fullResponse) {
        super("Gemini request blocked! Reason: " + reason.toUpperCase() + " | Full Response: " + fullResponse);
        this.reason = reason;
    }

    @NotNull
    public String getReason() {
        return this.reason;
    }

}
