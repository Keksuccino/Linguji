import de.keksuccino.linguji.linguji.backend.Backend;
import de.keksuccino.linguji.linguji.backend.engine.engines.openrouter.OpenRouterTranslationEngine;

public class TestOpenRouterNoKey {
    
    public static void main(String[] args) {
        // Initialize backend
        Backend.init();
        
        // Clear any existing API key
        Backend.getOptions().openRouterApiKey.setValue("");
        
        // Create engine instance - should not fail
        try {
            OpenRouterTranslationEngine engine = new OpenRouterTranslationEngine();
            
            // Check if key is valid
            System.out.println("API Key Valid: " + engine.isValidKey());
            
            // Get available models - should return default models
            System.out.println("Available Models: " + engine.getAvailableModels().size());
            engine.getAvailableModels().forEach(model -> {
                System.out.println("  - " + model.getDisplayName());
            });
            
            System.out.println("\nTest passed: Engine handles missing API key gracefully!");
            
        } catch (Exception e) {
            System.err.println("Test failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
