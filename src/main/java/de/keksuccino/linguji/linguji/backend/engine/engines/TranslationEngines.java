package de.keksuccino.linguji.linguji.backend.engine.engines;

import de.keksuccino.linguji.linguji.backend.Backend;
import de.keksuccino.linguji.linguji.backend.engine.SharedTranslatorOptions;
import de.keksuccino.linguji.linguji.backend.engine.TranslationEngineBuilder;
import de.keksuccino.linguji.linguji.backend.engine.engines.deepl.DeepLTranslationEngine;
import de.keksuccino.linguji.linguji.backend.engine.engines.deeplx.DeepLXTranslationEngine;
import de.keksuccino.linguji.linguji.backend.engine.engines.gemini.GeminiTranslationEngine;
import de.keksuccino.linguji.linguji.backend.engine.engines.libretranslate.LibreTranslationEngine;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class TranslationEngines {

    public static final TranslationEngineBuilder<GeminiTranslationEngine> GEMINI_PRO = new TranslationEngineBuilder<>(
            () -> new GeminiTranslationEngine(
                    Backend.getOptions().geminiApiKey.getValue(),
                    SharedTranslatorOptions.getPrompt(),
                    Objects.requireNonNull(SharedTranslatorOptions.getSourceLanguage()),
                    Objects.requireNonNull(SharedTranslatorOptions.getTargetLanguage())),
            () -> (!Backend.getOptions().geminiApiKey.getValue().trim().isEmpty() && !Backend.getOptions().aiPrompt.getValue().trim().isEmpty()),
            "gemini_pro", "Gemini Pro");

    public static final TranslationEngineBuilder<DeepLTranslationEngine> DEEPL = new TranslationEngineBuilder<>(
            () -> new DeepLTranslationEngine(
                    Backend.getOptions().deepLApiKey.getValue(),
                    Objects.requireNonNull(SharedTranslatorOptions.getSourceLanguage()),
                    Objects.requireNonNull(SharedTranslatorOptions.getTargetLanguage())),
            () -> !Backend.getOptions().deepLApiKey.getValue().trim().isEmpty(),
            "deepl", "DeepL");

    public static final TranslationEngineBuilder<DeepLXTranslationEngine> DEEPLX = new TranslationEngineBuilder<>(
            () -> new DeepLXTranslationEngine(
                    Objects.requireNonNull(SharedTranslatorOptions.getSourceLanguage()),
                    Objects.requireNonNull(SharedTranslatorOptions.getTargetLanguage())),
            () -> !Backend.getOptions().deepLxUrl.getValue().trim().isEmpty(),
            "deeplx", "DeepLX");

    public static final TranslationEngineBuilder<LibreTranslationEngine> LIBRE_TRANSLATE = new TranslationEngineBuilder<>(
            () -> new LibreTranslationEngine(
                    Backend.getOptions().libreTranslateUrl.getValue(),
                    Backend.getOptions().libreTranslateApiKey.getValue(),
                    Objects.requireNonNull(SharedTranslatorOptions.getSourceLanguage()),
                    Objects.requireNonNull(SharedTranslatorOptions.getTargetLanguage())),
            () -> !Backend.getOptions().libreTranslateUrl.getValue().trim().isEmpty(),
            "libre_translate", "Libre Translate");

    @NotNull
    public static List<TranslationEngineBuilder<?>> getBuilders() {
        return List.of(GEMINI_PRO, DEEPL, DEEPLX, LIBRE_TRANSLATE);
    }

    @NotNull
    public static List<TranslationEngineBuilder<?>> getReadyBuilders() {
        List<TranslationEngineBuilder<?>> builders = new ArrayList<>();
        for (TranslationEngineBuilder<?> b : getBuilders()) {
            if (b.isTranslatorReady()) builders.add(b);
        }
        return builders;
    }

    @Nullable
    public static TranslationEngineBuilder<?> getByName(@NotNull String name) {
        for (TranslationEngineBuilder<?> b : getBuilders()) {
            if (b.getName().equals(name)) return b;
        }
        return null;
    }

    @Nullable
    public static TranslationEngineBuilder<?> getByDisplayName(@NotNull String displayName) {
        for (TranslationEngineBuilder<?> b : getBuilders()) {
            if (b.getDisplayName().equals(displayName)) return b;
        }
        return null;
    }

}
