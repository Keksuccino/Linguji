package de.keksuccino.linguji.linguji.backend.brain.brains;

import de.keksuccino.linguji.linguji.backend.brain.AbstractTranslationBrain;
import de.keksuccino.linguji.linguji.backend.engine.AbstractTranslationEngine;
import de.keksuccino.linguji.linguji.backend.subtitle.subtitles.AbstractSubtitle;
import de.keksuccino.linguji.linguji.backend.subtitle.subtitles.SrtSubtitle;
import de.keksuccino.linguji.linguji.backend.subtitle.translation.SubtitleTranslator;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.io.File;

public class SrtTranslationBrain extends AbstractTranslationBrain<SrtSubtitle> {

    @Override
    public @NotNull String getDisplayName() {
        return "SRT";
    }

    @Override
    public boolean checkFileCompatibility(@NotNull File subtitleFile) {
        return subtitleFile.isFile() && subtitleFile.getPath().toLowerCase().endsWith(".srt");
    }

    @Override
    public boolean checkSubtitleCompatibility(@NotNull AbstractSubtitle subtitle) {
        return subtitle instanceof SrtSubtitle;
    }

    @Override
    public @Nullable SrtSubtitle parseFile(@NotNull File subtitleFile) {
        return this.checkFileCompatibility(subtitleFile) ? SrtSubtitle.create(subtitleFile) : null;
    }

    @Override
    public @NotNull SubtitleTranslator<SrtSubtitle> createTranslatorInstance(@NotNull AbstractTranslationEngine primaryTranslationEngine, @NotNull AbstractTranslationEngine fallbackTranslationEngine) {
        return new SubtitleTranslator<>(primaryTranslationEngine, fallbackTranslationEngine);
    }

}
