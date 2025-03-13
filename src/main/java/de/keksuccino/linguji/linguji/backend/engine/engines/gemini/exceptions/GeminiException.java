package de.keksuccino.linguji.linguji.backend.engine.engines.gemini.exceptions;

public class GeminiException extends Exception {

    public GeminiException(String message) {
        super(message);
    }

    public GeminiException(Exception ex) {
        super(ex);
    }

}
