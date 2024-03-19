package de.keksuccino.linguji.linguji.backend.translator.azure;

import de.keksuccino.linguji.linguji.backend.subtitle.translation.TranslationProcess;
import de.keksuccino.linguji.linguji.backend.translator.ITranslationEngine;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.util.Objects;

/**
 * NOT READY YET !!!!!!!!!!!!!!!!!
 */
public class AzureTranslationEngine implements ITranslationEngine {

    public static final String AZURE_URL = "https://api.cognitive.microsofttranslator.com/translate?api-version=3.0";

    public final String clientTraceId;

    public AzureTranslationEngine(@NotNull String clientTraceId) {
        this.clientTraceId = Objects.requireNonNull(clientTraceId);
    }

    @Override
    public @Nullable String translate(@NotNull String text, @NotNull String sourceLanguage, @NotNull String targetLanguage, @NotNull TranslationProcess process) throws Exception {

//        if (!process.running) return null;

        return null;

    }

    @Override
    public @NotNull String getEngineName() {
        return "Azure Text Translation";
    }

    @Override
    public int getMaxCharacterLength() {
        return 0;
    }

    @Override
    public @NotNull String getRawPrompt() {
        return "";
    }

}
