package de.keksuccino.linguji.linguji.backend.translator;

import de.keksuccino.linguji.linguji.backend.subtitle.translation.TranslationProcess;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface ITranslationEngine {

    public static final String PROMPT_PLACEHOLDER_SOURCE_LANG = "%source_lang%";
    public static final String PROMPT_PLACEHOLDER_TARGET_LANG = "%target_lang%";
    public static final String PROMPT_PLACEHOLDER_TEXT_TO_TRANSLATE = "%text_to_translate%";
    public static final String DEFAULT_PROMPT = "Translate the following text from " + PROMPT_PLACEHOLDER_SOURCE_LANG + " to " + PROMPT_PLACEHOLDER_TARGET_LANG + ". Only translate " + PROMPT_PLACEHOLDER_SOURCE_LANG + " text, no other languages. There are \"<br>\" in the text, which are special line breaks. Don't remove these special \"<br>\" line breaks and don't add new ones. Don't merge normal text lines. Most parts of the text are conversations, so context is important. Reply only with the translation. This is the text:\n\n" + PROMPT_PLACEHOLDER_TEXT_TO_TRANSLATE;

    //TODO fill this with stuff or just remove it and add a config option for it instead
    public static final String[] PROFANITY_FILTER = new String[] {};

    @Nullable
    String translate(@NotNull String text, @NotNull String sourceLanguage, @NotNull String targetLanguage, @NotNull TranslationProcess process) throws Exception;

    @NotNull
    String getEngineName();

    int getMaxCharacterLength();

    @NotNull
    String getRawPrompt();

    @NotNull
    default String getFinalPrompt(@NotNull String text, @NotNull String sourceLanguage, @NotNull String targetLanguage) {
        String promptFinal = this.getRawPrompt().replace("\\n", "\n");
        promptFinal = promptFinal.replace(PROMPT_PLACEHOLDER_SOURCE_LANG, sourceLanguage);
        promptFinal = promptFinal.replace(PROMPT_PLACEHOLDER_TARGET_LANG, targetLanguage);
        promptFinal = promptFinal.replace(PROMPT_PLACEHOLDER_TEXT_TO_TRANSLATE, text);
        return promptFinal;
    }

    @NotNull
    default String replaceProfanity(@NotNull String text, @NotNull String replaceWith) {
        String filtered = text;
        for (String word : PROFANITY_FILTER) {
            filtered = StringUtils.replaceIgnoreCase(filtered, word, replaceWith);
        }
        return filtered;
    }

}
