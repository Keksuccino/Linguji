import de.keksuccino.linguji.linguji.backend.Backend;
import de.keksuccino.linguji.linguji.backend.engine.engines.openrouter.OpenRouterTranslationEngine;
import de.keksuccino.linguji.linguji.backend.subtitle.translation.TranslationProcess;
import de.keksuccino.linguji.linguji.backend.lib.lang.Locale;

public class TestOpenRouter {
    
    public static void main(String[] args) {
        // Initialize backend
        Backend.init();
        
        // Set a test API key
        Backend.getOptions().openRouterApiKey.setValue("YOUR_API_KEY_HERE");
        
        // Create engine instance
        OpenRouterTranslationEngine engine = new OpenRouterTranslationEngine();
        
        // Check if key is valid
        System.out.println("API Key Valid: " + engine.isValidKey());
        
        // Get available models
        System.out.println("Available Models: " + engine.getAvailableModels().size());
        engine.getAvailableModels().forEach(model -> {
            System.out.println("  - " + model.getDisplayName());
        });
        
        // Test translation
        try {
            String testText = "Hello, world!";
            Locale sourceLang = Locale.getByName("english");
            Locale targetLang = Locale.getByName("german");
            
            // Update engine languages
            engine.sourceLanguage = sourceLang;
            engine.targetLanguage = targetLang;
            
            // Create a dummy translation process
            TranslationProcess process = new TranslationProcess();
            process.running = true;
            
            String translated = engine.translate(testText, process);
            System.out.println("Translation: " + testText + " -> " + translated);
        } catch (Exception e) {
            System.err.println("Translation failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
