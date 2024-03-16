package de.keksuccino.linguji.linguji.backend.translator.exceptions;

import de.keksuccino.linguji.linguji.backend.translator.ITranslationEngine;

/**
 * Used for when a request to or a response from an {@link ITranslationEngine} got blocked for safety reasons.
 */
public class SafetyException extends Exception {

    /**
     * Used for when a request to or a response from an {@link ITranslationEngine} got blocked for safety reasons.
     */
    public SafetyException(String message) {
        super(message);
    }

    /**
     * Used for when a request to or a response from an {@link ITranslationEngine} got blocked for safety reasons.
     */
    public SafetyException(Exception ex) {
        super(ex);
    }

}
