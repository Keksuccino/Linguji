package de.keksuccino.linguji.linguji.backend;

import de.keksuccino.linguji.linguji.backend.engine.engines.deepl.DeepLTranslationEngine;
import de.keksuccino.linguji.linguji.backend.lib.lang.Locale;
import de.keksuccino.linguji.linguji.backend.lib.logger.LogHandler;
import de.keksuccino.linguji.linguji.backend.lib.logger.SimpleLogger;
import de.keksuccino.linguji.linguji.backend.subtitle.translation.TranslationProcess;

/**
 * Simple test class for DeepL integration
 * Run this to test if DeepL API is working correctly
 */
public class TestDeepL {

    private static final SimpleLogger LOGGER = LogHandler.getLogger();

    public static void main(String[] args) {
        try {
            // Initialize backend
            Backend.init();
            
            LOGGER.info("=== DeepL Integration Test ===");
            
            // Get API key from config
            String apiKey = Backend.getOptions().deepLApiKey.getValue();
            if (apiKey == null || apiKey.trim().isEmpty()) {
                LOGGER.error("DeepL API key is not set in config.txt!");
                LOGGER.error("Please add your DeepL API key to config.txt with: deepl_api_key = YOUR_API_KEY_HERE");
                return;
            }
            
            LOGGER.info("DeepL API key found (length: " + apiKey.length() + ")");
            LOGGER.info("Using DeepL " + (Backend.getOptions().deepLUsePro.getValue() ? "Pro" : "Free") + " API");
            
            // Test different language combinations
            testTranslation(apiKey, Locale.ENGLISH, Locale.GERMAN, "Hello World!");
            testTranslation(apiKey, Locale.ENGLISH, Locale.SPANISH, "How are you today?");
            testTranslation(apiKey, Locale.GERMAN, Locale.ENGLISH, "Guten Tag!");
            testTranslation(apiKey, Locale.JAPANESE, Locale.ENGLISH, "こんにちは");
            
            LOGGER.info("=== Test Complete ===");
            
        } catch (Exception e) {
            LOGGER.error("Test failed with exception: ", e);
        }
    }
    
    private static void testTranslation(String apiKey, Locale source, Locale target, String text) {
        try {
            LOGGER.info("\n--- Testing: " + source.getDisplayName() + " -> " + target.getDisplayName() + " ---");
            LOGGER.info("Input text: " + text);
            
            // Create mock process
            TranslationProcess process = new TranslationProcess();
            process.running = true;
            
            // Create engine and translate
            DeepLTranslationEngine engine = new DeepLTranslationEngine(apiKey, source, target);
            String result = engine.translate(text, process);
            
            if (result != null) {
                LOGGER.info("SUCCESS! Translation: " + result);
            } else {
                LOGGER.error("FAILED! Translation returned null");
            }
            
            // Wait a bit between requests
            Thread.sleep(1000);
            
        } catch (Exception e) {
            LOGGER.error("Translation test failed: " + e.getMessage());
        }
    }
}
