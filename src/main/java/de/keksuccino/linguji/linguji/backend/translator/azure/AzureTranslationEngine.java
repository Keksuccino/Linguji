package de.keksuccino.linguji.linguji.backend.translator.azure;

import de.keksuccino.linguji.linguji.backend.subtitle.translation.TranslationProcess;
import de.keksuccino.linguji.linguji.backend.translator.AbstractTranslationEngine;
import de.keksuccino.linguji.linguji.backend.util.lang.LanguageType;
import de.keksuccino.linguji.linguji.backend.util.lang.Locale;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.util.Objects;

/**
 * NOT READY YET !!!!!!!!!!!!!!!!!
 */
public class AzureTranslationEngine extends AbstractTranslationEngine {

    public static final String AZURE_URL = "https://api.cognitive.microsofttranslator.com/translate?api-version=3.0";

    public final String clientTraceId;

    public AzureTranslationEngine(@NotNull String clientTraceId, @NotNull Locale sourceLanguage, @NotNull Locale targetLanguage) {
        super(null, sourceLanguage, targetLanguage);
        this.clientTraceId = Objects.requireNonNull(clientTraceId);
    }

    @Override
    public @Nullable String translate(@NotNull String text, @NotNull TranslationProcess process) throws Exception {
        return null;
    }

    @Override
    public @NotNull String getRawPrompt() {
        return "";
    }

    @Override
    public @NotNull LanguageType getLanguageType() {
        return LanguageType.ISO;
    }

}
