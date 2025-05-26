package de.keksuccino.linguji.linguji.backend.engine.engines.openrouter;

import de.keksuccino.linguji.linguji.backend.Backend;
import de.keksuccino.linguji.linguji.backend.engine.AbstractTranslationEngine;
import de.keksuccino.linguji.linguji.backend.engine.engines.TranslationEngines;
import de.keksuccino.linguji.linguji.backend.lib.lang.LanguageType;
import de.keksuccino.linguji.linguji.backend.lib.lang.Locale;
import de.keksuccino.linguji.linguji.backend.lib.logger.LogHandler;
import de.keksuccino.linguji.linguji.backend.lib.logger.SimpleLogger;
import de.keksuccino.linguji.linguji.backend.subtitle.translation.TranslationProcess;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class OpenRouterTranslationEngine extends AbstractTranslationEngine {

    private static final SimpleLogger LOGGER = LogHandler.getLogger();
    
    private OpenRouterTranslator translator;
    private List<OpenRouterModel> availableModels = new ArrayList<>();
    
    public OpenRouterTranslationEngine() {
        super(TranslationEngines.OPENROUTER, 
              Locale.getByName(Backend.getOptions().sourceLanguageLocale.getValue()) != null ? 
                  Locale.getByName(Backend.getOptions().sourceLanguageLocale.getValue()) : 
                  Locale.getByName("english"),
              Locale.getByName(Backend.getOptions().targetLanguageLocale.getValue()) != null ? 
                  Locale.getByName(Backend.getOptions().targetLanguageLocale.getValue()) : 
                  Locale.getByName("german"));
        this.refreshAvailableModels();
    }

    @Override
    public @Nullable String translate(@NotNull String text, @NotNull TranslationProcess process) throws Exception {
        if (!process.running) return null;
        
        if (!this.isValidKey()) {
            throw new Exception("OpenRouter API key is not configured. Please add your API key in the settings.");
        }
        
        if (translator == null) {
            translator = new OpenRouterTranslator();
        }
        
        this.startRequest();
        
        return translator.translate(text, sourceLanguage, targetLanguage);
    }

    public boolean isValidKey() {
        String apiKey = Backend.getOptions().openRouterApiKey.getValue();
        return apiKey != null && !apiKey.trim().isEmpty();
    }

    @Override
    public @NotNull String getRawPrompt() {
        return Backend.getOptions().aiPrompt.getValue();
    }

    @Override
    public @NotNull LanguageType getLanguageType() {
        return LanguageType.DISPLAY;
    }

    public void refreshAvailableModels() {
        if (!isValidKey()) {
            // Silently load default models when no API key
            this.availableModels = new OpenRouterModelFetcher("").getDefaultModels();
            return;
        }
        
        try {
            OpenRouterModelFetcher fetcher = new OpenRouterModelFetcher(Backend.getOptions().openRouterApiKey.getValue());
            List<OpenRouterModel> fetchedModels = fetcher.fetchAvailableModels();
            if (fetchedModels != null && !fetchedModels.isEmpty()) {
                this.availableModels = fetchedModels;
                LOGGER.info("Fetched " + availableModels.size() + " models from OpenRouter");
            } else {
                // Load default models if fetch returned null or empty
                this.availableModels = new OpenRouterModelFetcher("").getDefaultModels();
                LOGGER.warn("No models fetched from API, using defaults");
            }
        } catch (Exception e) {
            LOGGER.error("Failed to fetch OpenRouter models", e);
            // Load default models on error
            this.availableModels = new OpenRouterModelFetcher("").getDefaultModels();
        }
    }

    public List<OpenRouterModel> getAvailableModels() {
        if (availableModels == null) {
            availableModels = new ArrayList<>();
        }
        return new ArrayList<>(availableModels);
    }
}
