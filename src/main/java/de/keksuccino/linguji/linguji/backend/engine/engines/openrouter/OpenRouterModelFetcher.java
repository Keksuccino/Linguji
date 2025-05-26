package de.keksuccino.linguji.linguji.backend.engine.engines.openrouter;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import de.keksuccino.linguji.linguji.backend.lib.logger.LogHandler;
import de.keksuccino.linguji.linguji.backend.lib.logger.SimpleLogger;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.io.entity.EntityUtils;

import java.util.ArrayList;
import java.util.List;

public class OpenRouterModelFetcher {
    
    private static final SimpleLogger LOGGER = LogHandler.getLogger();
    private static final String MODELS_ENDPOINT = "https://openrouter.ai/api/v1/models";
    private static final String APP_URL = "https://github.com/Keksuccino/Linguji";
    private static final String APP_NAME = "Linguji";
    
    private final String apiKey;
    
    public OpenRouterModelFetcher(String apiKey) {
        this.apiKey = apiKey;
    }
    
    public List<OpenRouterModel> fetchAvailableModels() throws Exception {
        List<OpenRouterModel> models = new ArrayList<>();
        
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpGet request = new HttpGet(MODELS_ENDPOINT);
            request.addHeader("Authorization", "Bearer " + apiKey);
            request.addHeader("Content-Type", "application/json");
            request.addHeader("HTTP-Referer", APP_URL);
            request.addHeader("X-Title", APP_NAME);
            
            try (CloseableHttpResponse response = httpClient.execute(request)) {
                int statusCode = response.getCode();
                String responseBody = EntityUtils.toString(response.getEntity());
                
                if (statusCode != 200) {
                    LOGGER.error("Failed to fetch models from OpenRouter. Status: " + statusCode + ", Response: " + responseBody);
                    throw new Exception("Failed to fetch models: HTTP " + statusCode);
                }
                
                JsonObject jsonResponse = JsonParser.parseString(responseBody).getAsJsonObject();
                
                if (!jsonResponse.has("data")) {
                    // If no data field, try to use default models
                    return getDefaultModels();
                }
                
                JsonArray data = jsonResponse.getAsJsonArray("data");
                
                for (JsonElement element : data) {
                    try {
                        JsonObject modelJson = element.getAsJsonObject();
                        OpenRouterModel model = parseModel(modelJson);
                        if (model != null) {
                            models.add(model);
                        }
                    } catch (Exception e) {
                        LOGGER.warn("Failed to parse model: " + e.getMessage());
                    }
                }
            }
        }
        
        if (models.isEmpty()) {
            LOGGER.warn("No models fetched from API, using defaults");
            return getDefaultModels();
        }
        
        LOGGER.info("Successfully fetched " + models.size() + " models from OpenRouter");
        return models;
    }
    
    private OpenRouterModel parseModel(JsonObject modelJson) {
        String id = modelJson.get("id").getAsString();
        String name = modelJson.has("name") ? modelJson.get("name").getAsString() : id;
        
        int contextLength = 4096; // Default
        if (modelJson.has("context_length")) {
            contextLength = modelJson.get("context_length").getAsInt();
        }
        
        boolean supportsVision = false;
        boolean supportsFunctions = false;
        
        // Check capabilities
        if (modelJson.has("capabilities")) {
            JsonArray capabilities = modelJson.getAsJsonArray("capabilities");
            for (JsonElement cap : capabilities) {
                String capability = cap.getAsString();
                if ("vision".equals(capability)) {
                    supportsVision = true;
                } else if ("tools".equals(capability) || "function-calling".equals(capability)) {
                    supportsFunctions = true;
                }
            }
        }
        
        // Additional heuristics for vision support
        if (id.contains("vision") || id.contains("image") || 
            id.contains("gpt-4o") || id.contains("gemini") && id.contains("pro")) {
            supportsVision = true;
        }
        
        // Additional heuristics for function support
        if (id.contains("gpt-4") || id.contains("gpt-3.5") || 
            id.contains("claude-3") || id.contains("gemini")) {
            supportsFunctions = true;
        }
        
        return new OpenRouterModel(id, name, contextLength, supportsVision, supportsFunctions);
    }
    
    public List<OpenRouterModel> getDefaultModels() {
        List<OpenRouterModel> models = new ArrayList<>();
        
        // Add some popular default models
        models.add(new OpenRouterModel("openai/gpt-4o", "GPT-4o", 128000, true, true));
        models.add(new OpenRouterModel("openai/gpt-4-turbo", "GPT-4 Turbo", 128000, true, true));
        models.add(new OpenRouterModel("anthropic/claude-3-opus-20240229", "Claude 3 Opus", 200000, true, true));
        models.add(new OpenRouterModel("anthropic/claude-3-sonnet-20240229", "Claude 3 Sonnet", 200000, true, true));
        models.add(new OpenRouterModel("anthropic/claude-3-haiku-20240307", "Claude 3 Haiku", 200000, true, true));
        models.add(new OpenRouterModel("google/gemini-1.5-pro", "Gemini 1.5 Pro", 1000000, true, true));
        models.add(new OpenRouterModel("meta-llama/llama-3-70b-instruct", "Llama 3 70B", 8192, false, false));
        
        return models;
    }
}
