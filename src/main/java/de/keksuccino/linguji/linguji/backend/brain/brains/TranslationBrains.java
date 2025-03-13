package de.keksuccino.linguji.linguji.backend.brain.brains;

import de.keksuccino.linguji.linguji.backend.brain.AbstractTranslationBrain;
import org.jetbrains.annotations.NotNull;
import java.util.ArrayList;
import java.util.List;

public class TranslationBrains {

    private static final List<AbstractTranslationBrain<?>> BRAINS = new ArrayList<>();

    public static final AssTranslationBrain ASS_TRANSLATION_BRAIN = registerBrain(new AssTranslationBrain());
    public static final SrtTranslationBrain SRT_TRANSLATION_BRAIN = registerBrain(new SrtTranslationBrain());
    public static final I18nTranslationBrain I18N_TRANSLATION_BRAIN = registerBrain(new I18nTranslationBrain());

    @NotNull
    public static <B extends AbstractTranslationBrain<?>> B registerBrain(@NotNull B brain) {
        BRAINS.add(brain);
        return brain;
    }

    public static List<AbstractTranslationBrain<?>> getBrains() {
        return new ArrayList<>(BRAINS);
    }

}
