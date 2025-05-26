package de.keksuccino.linguji.linguji.backend.engine.engines.gemini;

import org.jetbrains.annotations.NotNull;

public class GeminiModel {
    
    private final String modelId;
    private final String displayName;
    private final boolean supportsVision;
    private final boolean supportsFunctions;
    
    public GeminiModel(@NotNull String modelId, @NotNull String displayName, 
                      boolean supportsVision, boolean supportsFunctions) {
        this.modelId = modelId;
        this.displayName = displayName;
        this.supportsVision = supportsVision;
        this.supportsFunctions = supportsFunctions;
    }
    
    @NotNull
    public String getModelId() {
        return modelId;
    }
    
    @NotNull
    public String getDisplayName() {
        return displayName;
    }
    
    public boolean supportsVision() {
        return supportsVision;
    }
    
    public boolean supportsFunctions() {
        return supportsFunctions;
    }
    
    @Override
    public String toString() {
        return displayName;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        GeminiModel that = (GeminiModel) obj;
        return modelId.equals(that.modelId);
    }
    
    @Override
    public int hashCode() {
        return modelId.hashCode();
    }
}
