package de.keksuccino.linguji.linguji.backend.brain;

import de.keksuccino.linguji.linguji.backend.subtitle.subtitles.AbstractSubtitle;
import de.keksuccino.linguji.linguji.backend.subtitle.translation.SubtitleTranslator;
import de.keksuccino.linguji.linguji.backend.subtitle.translation.TranslationProcess;
import de.keksuccino.linguji.linguji.backend.engine.AbstractTranslationEngine;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.io.File;

public abstract class AbstractTranslationBrain<S extends AbstractSubtitle> {

    @NotNull
    public abstract String getDisplayName();

    public abstract boolean checkFileCompatibility(@NotNull File subtitleFile);

    public abstract boolean checkSubtitleCompatibility(@NotNull AbstractSubtitle subtitle);

    @Nullable
    public abstract S parseFile(@NotNull File subtitleFile);

    @NotNull
    public abstract SubtitleTranslator<S> createTranslatorInstance(@NotNull AbstractTranslationEngine primaryTranslationEngine, @NotNull AbstractTranslationEngine fallbackTranslationEngine);

    @SuppressWarnings("unchecked")
    public void translate(@NotNull AbstractSubtitle subtitle, @NotNull TranslationProcess process, @NotNull AbstractTranslationEngine primaryTranslationEngine, @NotNull AbstractTranslationEngine fallbackTranslationEngine) throws Exception {
        if (this.checkSubtitleCompatibility(subtitle)) {
            SubtitleTranslator<S> translator = this.createTranslatorInstance(primaryTranslationEngine, fallbackTranslationEngine);
            translator.translate((S) subtitle, process);
        }
    }

}
