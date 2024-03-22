package de.keksuccino.linguji.linguji.backend.translator;

import de.keksuccino.linguji.linguji.backend.subtitle.translation.TranslationProcess;
import de.keksuccino.linguji.linguji.backend.util.lang.LanguageType;
import de.keksuccino.linguji.linguji.backend.util.lang.Locale;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.util.Objects;

public abstract class AbstractTranslationEngine {

    public static final String PROMPT_PLACEHOLDER_SOURCE_LANG = "%source_lang%";
    public static final String PROMPT_PLACEHOLDER_TARGET_LANG = "%target_lang%";
    public static final String PROMPT_PLACEHOLDER_TEXT_TO_TRANSLATE = "%text_to_translate%";
    public static final String DEFAULT_PROMPT = "Translate the following text from " + PROMPT_PLACEHOLDER_SOURCE_LANG + " to " + PROMPT_PLACEHOLDER_TARGET_LANG + ". Only translate " + PROMPT_PLACEHOLDER_SOURCE_LANG + " text, no other languages. There are \"<br>\" in the text, which are special line breaks. Don't remove these special \"<br>\" line breaks and don't add new ones. Don't merge normal text lines. Most parts of the text are conversations, so context is important. Reply only with the translation. This is the text:\n\n" + PROMPT_PLACEHOLDER_TEXT_TO_TRANSLATE;

    //TODO fill this with stuff or just remove it and add a config option for it instead
    public static final String[] PROFANITY_FILTER = new String[] {};

    @NotNull
    protected final TranslationEngineBuilder<?> builder;
    @NotNull
    public Locale sourceLanguage;
    @NotNull
    public Locale targetLanguage;

    public AbstractTranslationEngine(@NotNull TranslationEngineBuilder<?> builder, @NotNull Locale sourceLanguage, @NotNull Locale targetLanguage) {
        this.builder = Objects.requireNonNull(builder);
        this.sourceLanguage = Objects.requireNonNull(sourceLanguage);
        this.targetLanguage = Objects.requireNonNull(targetLanguage);
    }

    @NotNull
    public TranslationEngineBuilder<?> getBuilder() {
        return this.builder;
    }

    @Nullable
    public abstract String translate(@NotNull String text, @NotNull TranslationProcess process) throws Exception;

    @NotNull
    public abstract String getRawPrompt();

    @NotNull
    public abstract LanguageType getLanguageType();

    @NotNull
    public String getSourceLanguageString() {
        if (this.getLanguageType() == LanguageType.DISPLAY) {
            return this.sourceLanguage.getDisplayName();
        } else if (this.getLanguageType() == LanguageType.ISO) {
            return this.sourceLanguage.getIso();
        }
        return this.sourceLanguage.getIso3();
    }

    @NotNull
    public String getTargetLanguageString() {
        if (this.getLanguageType() == LanguageType.DISPLAY) {
            return this.targetLanguage.getDisplayName();
        } else if (this.getLanguageType() == LanguageType.ISO) {
            return this.targetLanguage.getIso();
        }
        return this.targetLanguage.getIso3();
    }

    @NotNull
    public String getFinalPrompt(@NotNull String text) {
        String promptFinal = this.getRawPrompt().replace("\\n", "\n");
        promptFinal = promptFinal.replace(PROMPT_PLACEHOLDER_SOURCE_LANG, this.getSourceLanguageString());
        promptFinal = promptFinal.replace(PROMPT_PLACEHOLDER_TARGET_LANG, this.getTargetLanguageString());
        promptFinal = promptFinal.replace(PROMPT_PLACEHOLDER_TEXT_TO_TRANSLATE, text);
        return promptFinal;
    }

    @NotNull
    public String replaceProfanity(@NotNull String text, @NotNull String replaceWith) {
        String filtered = text;
        for (String word : PROFANITY_FILTER) {
            filtered = StringUtils.replaceIgnoreCase(filtered, word, replaceWith);
        }
        return filtered;
    }

}
