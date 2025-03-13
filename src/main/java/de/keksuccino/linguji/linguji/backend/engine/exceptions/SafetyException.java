package de.keksuccino.linguji.linguji.backend.engine.exceptions;

import de.keksuccino.linguji.linguji.backend.engine.AbstractTranslationEngine;

/**
 * Used for when a request to or a response from an {@link AbstractTranslationEngine} got blocked for safety reasons.
 */
public class SafetyException extends Exception {

    /**
     * Used for when a request to or a response from an {@link AbstractTranslationEngine} got blocked for safety reasons.
     */
    public SafetyException(String message) {
        super(message);
    }

    /**
     * Used for when a request to or a response from an {@link AbstractTranslationEngine} got blocked for safety reasons.
     */
    public SafetyException(Exception ex) {
        super(ex);
    }

}
