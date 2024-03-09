package de.keksuccino.subtitleai;

import de.keksuccino.subtitleai.util.config.Config;
import de.keksuccino.subtitleai.util.options.AbstractOptions;

public class Options extends AbstractOptions {

    protected final Config config = new Config("config.txt");

    public final Option<String> aiPrompt = new Option<>(config, "ai_prompt", "Translate the following text from %source_lang% to %target_lang%. Only translate %source_lang% text, no other languages. There are \"<br>\" in the text, which are special line breaks. Don't remove these special \"<br>\" line breaks and don't add new ones. Don't merge normal text lines. Most parts of the text are conversations, so context is important. Reply only with the translation. This is the text:\\n\\n%text_to_translate%", "general");
    public final Option<Integer> linesPerPacket = new Option<>(config, "lines_per_packet", 15, "general");
    public final Option<Long> waitMillisBeforeNextTry = new Option<>(config, "wait_millis_before_next_try", 3000L, "general");
    public final Option<Integer> triesBeforeErrorInvalidLineCount = new Option<>(config, "tries_before_error_invalid_line_count", 2000, "general");
    public final Option<String> inputDirectory = new Option<>(config, "input_directory", "input_subtitles", "general");
    public final Option<String> outputDirectory = new Option<>(config, "output_directory", "output_subtitles", "general");
    public final Option<String> outputFileSuffix = new Option<>(config, "output_file_suffix", "", "general");
    public final Option<String> sourceLanguage = new Option<>(config, "source_language", "English", "general");
    public final Option<String> targetLanguage = new Option<>(config, "target_language", "German", "general");

    public final Option<String> geminiApiKey = new Option<>(config, "gemini_api_key", "", "gemini");
    public final Option<String> geminiHarmCategoryHarassmentSetting = new Option<>(config, "gemini_harm_category_harassment_setting", "BLOCK_NONE", "gemini");
    public final Option<String> geminiHarmCategoryHateSpeechSetting = new Option<>(config, "gemini_harm_category_hate_speech_setting", "BLOCK_ONLY_HIGH", "gemini");
    public final Option<String> geminiHarmCategorySexuallyExplicitSetting = new Option<>(config, "gemini_harm_category_sexually_explicit_setting", "BLOCK_ONLY_HIGH", "gemini");
    public final Option<String> geminiHarmCategoryDangerousContentSetting = new Option<>(config, "gemini_harm_category_dangerous_content_setting", "BLOCK_ONLY_HIGH", "gemini");
    public final Option<Integer> geminiTriesBeforeErrorSoftBlock = new Option<>(config, "gemini_tries_before_error_soft_block", 2000, "general");
    public final Option<Integer> geminiTriesBeforeErrorHardBlock = new Option<>(config, "gemini_tries_before_error_hard_block", 10, "general");
    public final Option<Integer> geminiTriesBeforeErrorGeneric = new Option<>(config, "gemini_tries_before_error_generic", 2000, "general");

    public Options() {
        this.config.syncConfig();
        this.config.clearUnusedValues();
    }

}
