package de.keksuccino.subtitleai.ai.exceptions;

public class ProfanityException extends Exception {

    public ProfanityException(String message) {
        super(message);
    }

    public ProfanityException(Exception ex) {
        super(ex);
    }

}
