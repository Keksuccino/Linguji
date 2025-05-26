package de.keksuccino.linguji.linguji.backend.engine.engines.openrouter;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import de.keksuccino.linguji.linguji.backend.Backend;
import de.keksuccino.linguji.linguji.backend.lib.lang.Locale;
import de.keksuccino.linguji.linguji.backend.lib.logger.LogHandler;
import de.keksuccino.linguji.linguji.backend.lib.logger.SimpleLogger;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.jetbrains.annotations.NotNull;

public class OpenRouterTranslator {
    
    private static final SimpleLogger LOGGER = LogHandler.getLogger();
    private static final String CHAT_ENDPOINT = "https://openrouter.ai/api/v1/chat/completions";
    private static final String APP_URL = "https://github.com/Keksuccino/Linguji";
    private static final String APP_NAME = "Linguji";
    
    public OpenRouterTranslator() {
    }
    
    public @NotNull String translate(@NotNull String text, @NotNull Locale sourceLocale, @NotNull Locale targetLocale) throws Exception {
        if (text.trim().isEmpty()) {
            return text;
        }
        
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpPost request = new HttpPost(CHAT_ENDPOINT);
            request.addHeader("Authorization", "Bearer " + Backend.getOptions().openRouterApiKey.getValue());
            request.addHeader("Content-Type", "application/json");
            request.addHeader("HTTP-Referer", APP_URL);
            request.addHeader("X-Title", APP_NAME);
            
            String requestBody = buildRequestJson(text, sourceLocale, targetLocale);
            request.setEntity(new StringEntity(requestBody, ContentType.APPLICATION_JSON));
            
            try (CloseableHttpResponse response = httpClient.execute(request)) {
                int statusCode = response.getCode();
                String responseBody = EntityUtils.toString(response.getEntity());
                
                if (statusCode != 200) {
                    handleErrorResponse(statusCode, responseBody);
                }
                
                return extractTranslatedText(responseBody);
            }
        } catch (Exception e) {
            LOGGER.error("Error during translation", e);
            throw new Exception("Translation failed: " + e.getMessage());
        }
    }
    
    private String buildRequestJson(String text, Locale sourceLocale, Locale targetLocale) {
        JsonObject json = new JsonObject();
        
        // Model
        json.addProperty("model", Backend.getOptions().openRouterModel.getValue());
        
        // Messages
        JsonArray messages = new JsonArray();
        
        // System message
        JsonObject systemMessage = new JsonObject();
        systemMessage.addProperty("role", "system");
        systemMessage.addProperty("content", buildSystemPrompt(sourceLocale, targetLocale));
        messages.add(systemMessage);
        
        // User message
        JsonObject userMessage = new JsonObject();
        userMessage.addProperty("role", "user");
        userMessage.addProperty("content", text);
        messages.add(userMessage);
        
        json.add("messages", messages);
        
        // Parameters
        double temperature = Backend.getOptions().openRouterTemperature.getValue();
        if (temperature < 0.0) temperature = 0.0;
        if (temperature > 2.0) temperature = 2.0;
        json.addProperty("temperature", temperature);
        json.addProperty("max_tokens", Backend.getOptions().openRouterMaxTokens.getValue());
        json.addProperty("top_p", Backend.getOptions().openRouterTopP.getValue());
        
        // OpenRouter specific parameters
        JsonArray transforms = new JsonArray(); // Empty array to disable transforms
        json.add("transforms", transforms);
        
        LOGGER.info("--> Sending to OpenRouter: " + json.toString());
        
        return json.toString();
    }
    
    private String buildSystemPrompt(Locale sourceLocale, Locale targetLocale) {
        String prompt = Backend.getOptions().aiPrompt.getValue();
        
        // Replace placeholders
        prompt = prompt.replace("%source_lang%", sourceLocale.getDisplayName());
        prompt = prompt.replace("%target_lang%", targetLocale.getDisplayName());
        
        // Note: %text_to_translate% will be handled by the calling code
        // For now, we'll remove it from the system prompt and just use it for context
        prompt = prompt.replace("%text_to_translate%", "").trim();
        
        return prompt;
    }
    
    private String extractTranslatedText(String responseBody) throws Exception {
        try {
            JsonObject response = JsonParser.parseString(responseBody).getAsJsonObject();
            
            // Trim the response before logging to remove empty lines
            LOGGER.info("<-- Response from OpenRouter: " + responseBody.trim());
            
            if (!response.has("choices") || response.getAsJsonArray("choices").isEmpty()) {
                throw new Exception("No translation choices in response");
            }
            
            JsonObject choice = response.getAsJsonArray("choices").get(0).getAsJsonObject();
            
            if (choice.has("message") && choice.getAsJsonObject("message").has("content")) {
                return choice.getAsJsonObject("message").get("content").getAsString();
            }
            
            throw new Exception("Could not extract translated text from response");
            
        } catch (Exception e) {
            LOGGER.error("Failed to parse response: " + responseBody, e);
            throw new Exception("Failed to parse translation response: " + e.getMessage());
        }
    }
    
    private void handleErrorResponse(int statusCode, String responseBody) throws Exception {
        String errorMessage = "OpenRouter API error (HTTP " + statusCode + ")";
        
        try {
            JsonObject errorJson = JsonParser.parseString(responseBody).getAsJsonObject();
            if (errorJson.has("error")) {
                JsonObject error = errorJson.getAsJsonObject("error");
                if (error.has("message")) {
                    errorMessage = error.get("message").getAsString();
                }
            }
        } catch (Exception e) {
            // Ignore parsing errors, use default message
        }
        
        LOGGER.error("OpenRouter API error: " + statusCode + " - " + responseBody);
        throw new Exception(errorMessage);
    }
}
