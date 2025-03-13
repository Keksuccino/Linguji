package de.keksuccino.linguji.linguji.backend.engine.engines.gemini.exceptions;

import de.keksuccino.linguji.linguji.backend.engine.exceptions.SafetyException;
import org.jetbrains.annotations.NotNull;

public class GeminiRequestHardBlockedException extends SafetyException {

    @NotNull
    public final String reason;

    public GeminiRequestHardBlockedException(@NotNull String reason, @NotNull String fullResponse) {
        super("Gemini request blocked! Reason: " + reason.toUpperCase() + " | Full Response: " + fullResponse);
        this.reason = reason;
    }

    @NotNull
    public String getReason() {
        return this.reason;
    }

}
