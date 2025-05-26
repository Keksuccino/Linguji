package de.keksuccino.linguji.linguji.backend.engine.engines.deepl;

import com.google.gson.Gson;
import de.keksuccino.linguji.linguji.backend.Backend;
import de.keksuccino.linguji.linguji.backend.engine.engines.deepl.response.DeepLResponse;
import de.keksuccino.linguji.linguji.backend.lib.HttpRequest;
import de.keksuccino.linguji.linguji.backend.lib.JsonUtils;
import de.keksuccino.linguji.linguji.backend.lib.lang.Locale;
import de.keksuccino.linguji.linguji.backend.lib.logger.LogHandler;
import de.keksuccino.linguji.linguji.backend.lib.logger.SimpleLogger;
import de.keksuccino.linguji.linguji.backend.subtitle.translation.TranslationProcess;
import org.apache.hc.client5.http.entity.EntityBuilder;
import org.apache.hc.core5.http.ContentType;

/**
 * Test class for debugging DeepL API integration
 */
public class DeepLTest {

    private static final SimpleLogger LOGGER = LogHandler.getLogger();

    public static void main(String[] args) {
        // Initialize backend
        Backend.init();
        
        // Test the DeepL API directly
        testDeepLAPI();
    }

    public static void testDeepLAPI() {
        try {
            LOGGER.info("=== DeepL API Test Starting ===");
            
            String apiKey = Backend.getOptions().deepLApiKey.getValue();
            if (apiKey == null || apiKey.trim().isEmpty()) {
                LOGGER.error("DeepL API key is not set!");
                return;
            }
            
            LOGGER.info("API Key found (length: " + apiKey.length() + ")");
            
            // Test translation
            Locale sourceLocale = Locale.ENGLISH;
            Locale targetLocale = Locale.GERMAN;
            String testText = "Hello, this is a test message.";
            
            LOGGER.info("Testing translation from " + sourceLocale.getDisplayName() + " to " + targetLocale.getDisplayName());
            LOGGER.info("Test text: " + testText);
            
            // Create a mock translation process
            TranslationProcess mockProcess = new TranslationProcess();
            mockProcess.running = true;
            
            // Create DeepL engine and test
            DeepLTranslationEngine engine = new DeepLTranslationEngine(apiKey, sourceLocale, targetLocale);
            
            // Direct API test first
            LOGGER.info("\n--- Direct API Test ---");
            testDirectAPI(apiKey, "EN", "DE", testText);
            
            // Engine test
            LOGGER.info("\n--- Engine Test ---");
            String result = engine.translate(testText, mockProcess);
            
            if (result != null) {
                LOGGER.info("Translation successful!");
                LOGGER.info("Result: " + result);
            } else {
                LOGGER.error("Translation failed - result was null");
            }
            
            LOGGER.info("=== DeepL API Test Complete ===");
            
        } catch (Exception e) {
            LOGGER.error("DeepL API test failed with exception: ", e);
        }
    }
    
    private static void testDirectAPI(String apiKey, String sourceLang, String targetLang, String text) {
        try {
            Gson gson = new Gson();
            
            String url = Backend.getOptions().deepLUsePro.getValue() ? DeepLTranslationEngine.DEEPL_PRO_URL : DeepLTranslationEngine.DEEPL_FREE_URL;
            LOGGER.info("Testing direct API call to: " + url);
            
            HttpRequest request = HttpRequest.create(url)
                    .addHeaderEntry("Content-Type", "application/json")
                    .addHeaderEntry("Authorization", "DeepL-Auth-Key " + apiKey);
            
            DeepLRequest deepLRequest = new DeepLRequest(sourceLang, targetLang, text);
            String json = gson.toJson(deepLRequest);
            
            LOGGER.info("Request JSON: " + json);
            
            EntityBuilder entityBuilder = EntityBuilder.create();
            entityBuilder.setContentEncoding("UTF-8");
            entityBuilder.setContentType(ContentType.APPLICATION_JSON);
            entityBuilder.setText(json);
            
            String responseString = JsonUtils.getJsonFromPOST(request, entityBuilder.build(), 15);
            LOGGER.info("Raw Response: " + responseString);
            
            if (responseString != null) {
                DeepLResponse response = gson.fromJson(responseString, DeepLResponse.class);
                if (response != null && response.translations != null && response.translations.length > 0) {
                    LOGGER.info("Direct API test successful! Translation: " + response.translations[0].text);
                } else {
                    LOGGER.error("Response parsing failed or no translations found");
                }
            }
            
        } catch (Exception e) {
            LOGGER.error("Direct API test failed: ", e);
        }
    }
}
