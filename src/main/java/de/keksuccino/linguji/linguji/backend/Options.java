package de.keksuccino.linguji.linguji.backend;

import de.keksuccino.linguji.linguji.backend.engine.FallbackTranslatorBehaviour;
import de.keksuccino.linguji.linguji.backend.engine.engines.TranslationEngines;
import de.keksuccino.linguji.linguji.backend.lib.config.Config;
import de.keksuccino.linguji.linguji.backend.lib.options.AbstractOptions;

public class Options extends AbstractOptions {

    protected final Config config = new Config("config.txt");

    public final Option<String> aiPrompt = new Option<>(config, "ai_prompt", "Translate the following text from %source_lang% to %target_lang%. Only translate %source_lang% text, no other languages. There are \"<br>\" in the text, which are special line breaks. Don't remove these special \"<br>\" line breaks and don't add new ones. Don't merge normal text lines. Most parts of the text are conversations, so context is important. Reply only with the translation. This is the text:\\n\\n%text_to_translate%", "general");
    public final Option<Integer> linesPerPacket = new Option<>(config, "lines_per_packet", 15, "general");
    /** Min should be 1 **/
    public final Option<Integer> triesBeforeErrorInvalidLineCount = new Option<>(config, "tries_before_error_invalid_line_count", 2000, "general");
    /** Min should be 1 **/
    public final Option<Integer> triesBeforeErrorTimeoutOrConnectionFailed = new Option<>(config, "tries_before_error_timeout_or_connection_failed", 2000, "general");
    /** Min should be 1 **/
    public final Option<Integer> triesBeforeErrorGeneric = new Option<>(config, "tries_before_error_generic", 2000, "general");
    public final Option<String> inputDirectory = new Option<>(config, "input_directory", "input_subtitles", "general");
    public final Option<String> outputDirectory = new Option<>(config, "output_directory", "output_subtitles", "general");
    public final Option<String> outputFileSuffix = new Option<>(config, "output_file_suffix", "", "general");
    public final Option<String> sourceLanguageLocale = new Option<>(config, "source_language_locale", "english", "general");
    public final Option<String> targetLanguageLocale = new Option<>(config, "target_language_locale", "german", "general");
    public final Option<String> primaryTranslationEngine = new Option<>(config, "primary_translation_engine", TranslationEngines.GEMINI_PRO.getName(), "general");
    public final Option<String> fallbackTranslationEngine = new Option<>(config, "fallback_translation_engine", TranslationEngines.DEEPLX.getName(), "general");
    public final Option<String> fallbackTranslatorBehaviour = new Option<>(config, "fallback_translator_behaviour", FallbackTranslatorBehaviour.TRANSLATE_FULL_PACKET.getName(), "general");
    public final Option<Long> waitMillisBetweenRequests = new Option<>(config, "wait_millis_between_requests", 4000L, "general");
    public final Option<Boolean> setVideoSubtitleAsDefault = new Option<>(config, "set_video_subtitle_as_default", true, "general");

    public final Option<String> geminiApiKey = new Option<>(config, "gemini_api_key", "", "gemini");
    public final Option<String> geminiHarmCategoryHarassmentSetting = new Option<>(config, "gemini_harm_category_harassment_setting", "BLOCK_ONLY_HIGH", "gemini");
    public final Option<String> geminiHarmCategoryHateSpeechSetting = new Option<>(config, "gemini_harm_category_hate_speech_setting", "BLOCK_ONLY_HIGH", "gemini");
    public final Option<String> geminiHarmCategorySexuallyExplicitSetting = new Option<>(config, "gemini_harm_category_sexually_explicit_setting", "BLOCK_ONLY_HIGH", "gemini");
    public final Option<String> geminiHarmCategoryDangerousContentSetting = new Option<>(config, "gemini_harm_category_dangerous_content_setting", "BLOCK_ONLY_HIGH", "gemini");
    /** Min should be 1 **/
    public final Option<Integer> geminiTriesBeforeErrorSoftBlock = new Option<>(config, "gemini_tries_before_error_soft_block", 2000, "gemini");
    /** Min should be 1 **/
    public final Option<Integer> geminiTriesBeforeErrorHardBlock = new Option<>(config, "gemini_tries_before_error_hard_block", 5, "gemini");
    public final Option<Boolean> geminiOverrideSafetyThresholdSoftBlock = new Option<>(config, "gemini_override_safety_threshold_soft_block", true, "gemini");
    public final Option<Boolean> geminiOverrideSafetyThresholdHardBlock = new Option<>(config, "gemini_override_safety_threshold_hard_block", true, "gemini");
    /** Min should be 1 **/
    public final Option<Integer> geminiOverrideSafetyThresholdSoftBlockAfterTries = new Option<>(config, "gemini_override_safety_threshold_soft_block_after_tries", 10, "gemini");
    /** Min should be 1 **/
    public final Option<Integer> geminiOverrideSafetyThresholdHardBlockAfterTries = new Option<>(config, "gemini_override_safety_threshold_hard_block_after_tries", 1, "gemini");
    /** Min should be 1 **/
    public final Option<Integer> geminiOverrideSafetyThresholdSoftBlockTriesPerLevel = new Option<>(config, "gemini_override_safety_threshold_soft_block_tries_per_level", 10, "gemini");
    /** Min should be 1 **/
    public final Option<Integer> geminiOverrideSafetyThresholdHardBlockTriesPerLevel = new Option<>(config, "gemini_override_safety_threshold_hard_block_tries_per_level", 2, "gemini");
    public final Option<Boolean> geminiOverrideSafetyThresholdSkipLowLevels = new Option<>(config, "gemini_override_safety_threshold_skip_low_levels", true, "gemini");
    public final Option<String> geminiModel = new Option<>(config, "gemini_model", "gemini-2.0-flash", "gemini");

    public final Option<String> libreTranslateUrl = new Option<>(config, "libre_translate_url", "https://trans.zillyhuhn.com/translate", "libre_translate");
    public final Option<String> libreTranslateApiKey = new Option<>(config, "libre_translate_api_key", "", "libre_translate");

    public final Option<String> deepLApiKey = new Option<>(config, "deepl_api_key", "", "deepl");
    public final Option<Boolean> deepLUsePro = new Option<>(config, "deepl_use_pro", false, "deepl");

    public final Option<String> deepLxUrl = new Option<>(config, "deeplx_url", "http://localhost:1188/translate", "deeplx");
    public final Option<Integer> deepLxTriesBeforeErrorEmptyResponse = new Option<>(config, "deeplx_tries_before_error_empty_response", 10, "deeplx");

    public final Option<String> openRouterApiKey = new Option<>(config, "openrouter_api_key", "", "openrouter");
    public final Option<String> openRouterModel = new Option<>(config, "openrouter_model", "openai/gpt-4o", "openrouter");
    public final Option<Double> openRouterTemperature = new Option<>(config, "openrouter_temperature", 0.3, "openrouter");
    public final Option<Integer> openRouterMaxTokens = new Option<>(config, "openrouter_max_tokens", 4096, "openrouter");
    public final Option<Double> openRouterTopP = new Option<>(config, "openrouter_top_p", 1.0, "openrouter");

    public Options() {
        this.config.syncConfig();
        this.config.clearUnusedValues();
    }

}
