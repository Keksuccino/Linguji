package de.keksuccino.linguji.linguji.backend.translator;

import de.keksuccino.linguji.linguji.backend.Backend;
import de.keksuccino.linguji.linguji.backend.util.lang.Locale;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SharedTranslatorOptions {

    @NotNull
    public static String getPrompt() {
        return Backend.getOptions().aiPrompt.getValue().replace("\\n", "\n");
    }

    @Nullable
    public static Locale getSourceLanguage() {
        return Locale.getByName(Backend.getOptions().sourceLanguageLocale.getValue());
    }

    @Nullable
    public static Locale getTargetLanguage() {
        return Locale.getByName(Backend.getOptions().targetLanguageLocale.getValue());
    }

    @Nullable
    public static TranslationEngineBuilder<?> getPrimaryTranslationEngine() {
        return TranslationEngines.getByName(Backend.getOptions().primaryTranslationEngine.getValue());
    }

    @Nullable
    public static TranslationEngineBuilder<?> getFallbackTranslationEngine() {
        return TranslationEngines.getByName(Backend.getOptions().fallbackTranslationEngine.getValue());
    }

    @Nullable
    public static FallbackTranslatorBehaviour getFallbackTranslatorBehaviour() {
        return FallbackTranslatorBehaviour.getByName(Backend.getOptions().fallbackTranslatorBehaviour.getValue());
    }

}
