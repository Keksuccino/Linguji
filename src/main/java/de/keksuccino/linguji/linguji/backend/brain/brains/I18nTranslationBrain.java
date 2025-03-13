package de.keksuccino.linguji.linguji.backend.brain.brains;

import de.keksuccino.linguji.linguji.backend.brain.AbstractTranslationBrain;
import de.keksuccino.linguji.linguji.backend.engine.AbstractTranslationEngine;
import de.keksuccino.linguji.linguji.backend.subtitle.subtitles.AbstractSubtitle;
import de.keksuccino.linguji.linguji.backend.subtitle.subtitles.JsonI18nLocalizationFile;
import de.keksuccino.linguji.linguji.backend.subtitle.translation.SubtitleTranslator;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.io.File;

public class I18nTranslationBrain extends AbstractTranslationBrain<JsonI18nLocalizationFile> {

    @Override
    public @NotNull String getDisplayName() {
        return "I18n Localization";
    }

    @Override
    public boolean checkFileCompatibility(@NotNull File subtitleFile) {
        return subtitleFile.isFile() && subtitleFile.getPath().toLowerCase().endsWith(".json");
    }

    @Override
    public boolean checkSubtitleCompatibility(@NotNull AbstractSubtitle subtitle) {
        return subtitle instanceof JsonI18nLocalizationFile;
    }

    @Override
    public @Nullable JsonI18nLocalizationFile parseFile(@NotNull File subtitleFile) {
        return this.checkFileCompatibility(subtitleFile) ? JsonI18nLocalizationFile.create(subtitleFile) : null;
    }

    @Override
    public @NotNull SubtitleTranslator<JsonI18nLocalizationFile> createTranslatorInstance(@NotNull AbstractTranslationEngine primaryTranslationEngine, @NotNull AbstractTranslationEngine fallbackTranslationEngine) {
        return new SubtitleTranslator<>(primaryTranslationEngine, fallbackTranslationEngine);
    }

}
