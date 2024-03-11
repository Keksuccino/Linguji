package de.keksuccino.polyglot.polyglot.backend.translator.gemini.exceptions;

import de.keksuccino.polyglot.polyglot.backend.translator.exceptions.SafetyException;
import org.jetbrains.annotations.NotNull;

public class GeminiRequestBlockedException extends SafetyException {

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
