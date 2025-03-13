package de.keksuccino.linguji.linguji.backend.brain.brains;

import de.keksuccino.linguji.linguji.backend.brain.AbstractTranslationBrain;
import de.keksuccino.linguji.linguji.backend.engine.AbstractTranslationEngine;
import de.keksuccino.linguji.linguji.backend.subtitle.subtitles.AbstractSubtitle;
import de.keksuccino.linguji.linguji.backend.subtitle.subtitles.AssSubtitle;
import de.keksuccino.linguji.linguji.backend.subtitle.translation.SubtitleTranslator;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.io.File;

public class AssTranslationBrain extends AbstractTranslationBrain<AssSubtitle> {

    @Override
    public @NotNull String getDisplayName() {
        return "ASS";
    }

    @Override
    public boolean checkFileCompatibility(@NotNull File subtitleFile) {
        return subtitleFile.isFile() && subtitleFile.getPath().toLowerCase().endsWith(".ass");
    }

    @Override
    public boolean checkSubtitleCompatibility(@NotNull AbstractSubtitle subtitle) {
        return subtitle instanceof AssSubtitle;
    }

    @Override
    public @Nullable AssSubtitle parseFile(@NotNull File subtitleFile) {
        return this.checkFileCompatibility(subtitleFile) ? AssSubtitle.create(subtitleFile) : null;
    }

    @Override
    public @NotNull SubtitleTranslator<AssSubtitle> createTranslatorInstance(@NotNull AbstractTranslationEngine primaryTranslationEngine, @NotNull AbstractTranslationEngine fallbackTranslationEngine) {
        return new SubtitleTranslator<>(primaryTranslationEngine, fallbackTranslationEngine);
    }

}
