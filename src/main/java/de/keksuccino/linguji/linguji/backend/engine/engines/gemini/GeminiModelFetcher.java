package de.keksuccino.linguji.linguji.backend.engine.engines.gemini;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import de.keksuccino.linguji.linguji.backend.lib.HttpRequest;
import de.keksuccino.linguji.linguji.backend.lib.JsonUtils;
import de.keksuccino.linguji.linguji.backend.lib.logger.LogHandler;
import de.keksuccino.linguji.linguji.backend.lib.logger.SimpleLogger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.util.ArrayList;
import java.util.List;

public class GeminiModelFetcher {
    
    private static final SimpleLogger LOGGER = LogHandler.getLogger();
    private static final String API_URL_BASE = "https://generativelanguage.googleapis.com/v1beta/models";
    
    /**
     * Fetches available Gemini models from the API
     * @param apiKey The Gemini API key
     * @return List of available models, or null if fetching fails
     */
    @Nullable
    public static List<GeminiModel> fetchAvailableModels(@NotNull String apiKey) {
        try {
            LOGGER.info("Fetching available Gemini models from API");
            
            HttpRequest request = HttpRequest.create(API_URL_BASE + "?key=" + apiKey)
                    .addHeaderEntry("Content-Type", "application/json")
                    .addHeaderEntry("accept", "application/json");
            
            String responseString = JsonUtils.getJsonFromGET(request, null, 10);
            
            if (responseString == null || responseString.isEmpty()) {
                LOGGER.warn("Empty response when fetching Gemini models");
                return null;
            }
            
            Gson gson = new Gson();
            JsonObject response = gson.fromJson(responseString, JsonObject.class);
            
            if (!response.has("models")) {
                LOGGER.warn("No models array in Gemini API response");
                return null;
            }
            
            JsonArray modelsArray = response.getAsJsonArray("models");
            List<GeminiModel> models = new ArrayList<>();
            
            for (int i = 0; i < modelsArray.size(); i++) {
                JsonObject modelInfo = modelsArray.get(i).getAsJsonObject();
                String fullModelName = modelInfo.get("name").getAsString();
                String modelId = fullModelName;
                
                // Extract just the model name from path format (models/gemini-1.5-pro)
                if (modelId.contains("/")) {
                    modelId = modelId.substring(modelId.lastIndexOf("/") + 1);
                }
                
                // Only include Gemini models
                if (modelId.startsWith("gemini-")) {
                    String displayName = beautifyModelName(modelId);
                    
                    // Determine capabilities based on model name (simplified approach)
                    boolean supportsVision = modelId.contains("vision") || 
                                           modelId.contains("2.0") || 
                                           modelId.contains("2.5") ||
                                           modelId.contains("1.5");
                    
                    boolean supportsFunctions = modelId.contains("1.5") || 
                                              modelId.contains("2.0") || 
                                              modelId.contains("2.5");
                    
                    models.add(new GeminiModel(modelId, displayName, supportsVision, supportsFunctions));
                    LOGGER.info("Found Gemini model: " + modelId + " (vision: " + supportsVision + ", functions: " + supportsFunctions + ")");
                }
            }
            
            LOGGER.info("Successfully fetched " + models.size() + " Gemini models");
            return models;
            
        } catch (Exception e) {
            LOGGER.error("Error fetching Gemini models: " + e.getMessage(), e);
            return null;
        }
    }
    
    /**
     * Returns a list of fallback models when API fetching fails
     */
    @NotNull
    public static List<GeminiModel> getFallbackModels() {
        List<GeminiModel> models = new ArrayList<>();
        
        // Add commonly available Gemini models as fallbacks
        models.add(new GeminiModel("gemini-2.0-flash", "Gemini 2.0 Flash", true, true));
        models.add(new GeminiModel("gemini-2.0-pro", "Gemini 2.0 Pro", true, true));
        models.add(new GeminiModel("gemini-1.5-flash", "Gemini 1.5 Flash", true, true));
        models.add(new GeminiModel("gemini-1.5-pro", "Gemini 1.5 Pro", true, true));
        models.add(new GeminiModel("gemini-1.0-pro", "Gemini 1.0 Pro", false, false));
        models.add(new GeminiModel("gemini-1.0-pro-vision", "Gemini 1.0 Pro Vision", true, false));
        
        return models;
    }
    
    /**
     * Creates a more user-friendly display name from model ID
     */
    private static String beautifyModelName(String modelId) {
        // Remove any version suffixes
        String cleanId = modelId.replaceAll("-\\d{8}", "");
        
        // Replace hyphens with spaces and capitalize
        String[] parts = cleanId.split("-");
        StringBuilder nameBuilder = new StringBuilder();
        
        for (int i = 0; i < parts.length; i++) {
            String part = parts[i];
            if (part.isEmpty()) continue;
            
            if (i == 0) {
                // Capitalize first word (Gemini)
                nameBuilder.append(part.substring(0, 1).toUpperCase())
                           .append(part.substring(1));
            } else if (part.matches("\\d+\\.\\d+")) {
                // This is a version number
                nameBuilder.append(" ").append(part);
            } else {
                // Capitalize first letter of other parts
                nameBuilder.append(" ")
                           .append(part.substring(0, 1).toUpperCase())
                           .append(part.substring(1));
            }
        }
        
        return nameBuilder.toString().trim();
    }
}
