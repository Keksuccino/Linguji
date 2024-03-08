package de.keksuccino.subtitleai.ai;

import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

public interface AiTranslator {

    public static final String PROMPT_PLACEHOLDER_SOURCE_LANG = "%source_lang%";
    public static final String PROMPT_PLACEHOLDER_TARGET_LANG = "%target_lang%";
    public static final String PROMPT_PLACEHOLDER_TEXT_TO_TRANSLATE = "%text_to_translate%";
    public static final String DEFAULT_PROMPT = "Translate the following text from " + PROMPT_PLACEHOLDER_SOURCE_LANG + " to " + PROMPT_PLACEHOLDER_TARGET_LANG + ". Only translate " + PROMPT_PLACEHOLDER_SOURCE_LANG + " text, no other languages. There are \"<br>\" in the text, which are special line breaks. Don't remove these special \"<br>\" line breaks and don't add new ones. Don't merge normal text lines. Most parts of the text are conversations, so context is important. Reply only with the translation. This is the text:\n\n" + PROMPT_PLACEHOLDER_TEXT_TO_TRANSLATE;

    public static final String[] PROFANITY_FILTER = new String[] { "cum", "sperm", "masturbate", "masturbating", "masturbated", "piss", "pissing", "kill", "stab", "stabbing", "sex", "sexual", "sexually", "shit", "fuck", "fucking", "frickin", "asshole", "bitch", "whore", "piece of shit", "piece of crap", "piece of garbage", "retard", "degenerate", "nigger", "nigga", "vagina", "penis", "pussy", "dick", "doggy style" };

    @NotNull
    String translate(@NotNull String text, @NotNull String sourceLanguage, @NotNull String targetLanguage) throws Exception;

    int getMaxCharacterLength();

    @NotNull
    String getRawPrompt();

    @NotNull
    default String getFinalPrompt(@NotNull String text, @NotNull String sourceLanguage, @NotNull String targetLanguage) {
        String promptFinal = this.getRawPrompt();
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
