package de.keksuccino.subtitleai.ai.gemini.exceptions;

public class GeminiException extends Exception {

    public GeminiException(String message) {
        super(message);
    }

    public GeminiException(Exception ex) {
        super(ex);
    }

}
