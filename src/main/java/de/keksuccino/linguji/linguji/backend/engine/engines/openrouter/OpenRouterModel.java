package de.keksuccino.linguji.linguji.backend.engine.engines.openrouter;

public class OpenRouterModel {
    
    private final String id;
    private final String name;
    private final int contextLength;
    private final boolean supportsVision;
    private final boolean supportsFunctions;
    
    public OpenRouterModel(String id, String name, int contextLength, 
                          boolean supportsVision, boolean supportsFunctions) {
        this.id = id;
        this.name = name;
        this.contextLength = contextLength;
        this.supportsVision = supportsVision;
        this.supportsFunctions = supportsFunctions;
    }
    
    public String getId() {
        return id;
    }
    
    public String getName() {
        return name;
    }
    
    public String getDisplayName() {
        return name + " (" + id + ")";
    }
    
    public int getContextLength() {
        return contextLength;
    }
    
    public boolean supportsVision() {
        return supportsVision;
    }
    
    public boolean supportsFunctions() {
        return supportsFunctions;
    }
    
    @Override
    public String toString() {
        return getDisplayName();
    }
}
