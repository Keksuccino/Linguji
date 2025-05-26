package de.keksuccino.linguji.linguji.backend.engine.engines.deepl.response;

/**
 * Represents an error response from the DeepL API
 */
public class DeepLErrorResponse {
    public String message;
    public String detail;
    
    @Override
    public String toString() {
        return "DeepLError{message='" + message + "', detail='" + detail + "'}";
    }
}
