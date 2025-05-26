package de.keksuccino.linguji.linguji.backend.engine.engines.deepl;

import com.google.gson.Gson;
import de.keksuccino.linguji.linguji.backend.Backend;
import de.keksuccino.linguji.linguji.backend.subtitle.translation.TranslationProcess;
import de.keksuccino.linguji.linguji.backend.engine.AbstractTranslationEngine;
import de.keksuccino.linguji.linguji.backend.engine.engines.TranslationEngines;
import de.keksuccino.linguji.linguji.backend.lib.lang.LanguageType;
import de.keksuccino.linguji.linguji.backend.engine.engines.deepl.response.DeepLResponse;
import de.keksuccino.linguji.linguji.backend.lib.HttpRequest;
import de.keksuccino.linguji.linguji.backend.lib.JsonUtils;
import de.keksuccino.linguji.linguji.backend.lib.lang.Locale;
import de.keksuccino.linguji.linguji.backend.lib.logger.LogHandler;
import de.keksuccino.linguji.linguji.backend.lib.logger.SimpleLogger;
import org.apache.hc.client5.http.entity.EntityBuilder;
import org.apache.hc.core5.http.ContentType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.util.Objects;

/**
 * DeepL Translation Engine implementation
 */
public class DeepLTranslationEngine extends AbstractTranslationEngine {

    private static final SimpleLogger LOGGER = LogHandler.getLogger();

    public static final String DEEPL_FREE_URL = "https://api-free.deepl.com/v2/translate";
    public static final String DEEPL_PRO_URL = "https://api.deepl.com/v2/translate";

    @NotNull
    public final String apiKey;

    public DeepLTranslationEngine(@NotNull String apiKey, @NotNull Locale sourceLanguage, @NotNull Locale targetLanguage) {
        super(TranslationEngines.DEEPL, sourceLanguage, targetLanguage);
        this.apiKey = Objects.requireNonNull(apiKey);
    }

    @Override
    public @Nullable String translate(@NotNull String text, @NotNull TranslationProcess process) throws Exception {
        return this._translate(text, 0, process);
    }

    protected @Nullable String _translate(@NotNull String text, int timeoutTries, @NotNull TranslationProcess process) throws Exception {

        if (!process.running) return null;

        Gson gson = new Gson();

        // Use Pro or Free URL based on configuration
        String apiUrl = Backend.getOptions().deepLUsePro.getValue() ? DEEPL_PRO_URL : DEEPL_FREE_URL;
        LOGGER.info("Using DeepL API endpoint: " + apiUrl);

        HttpRequest request = HttpRequest.create(apiUrl)
                .addHeaderEntry("Content-Type", "application/json")
                .addHeaderEntry("Authorization", "DeepL-Auth-Key " + this.apiKey);

        EntityBuilder entityBuilder = EntityBuilder.create();
        entityBuilder.setContentEncoding("UTF-8");
        entityBuilder.setContentType(ContentType.APPLICATION_JSON);

        String sourceLangCode = convertToDeepLLanguageCode(this.getSourceLanguageString());
        String targetLangCode = convertToDeepLLanguageCode(this.getTargetLanguageString());

        String json = Objects.requireNonNull(gson.toJson(new DeepLRequest(sourceLangCode, targetLangCode, text)));

        LOGGER.info("--> Sending to DeepL: " + json);

        entityBuilder.setText(json);

        if (!process.running) return null;

        String responseString;

        this.startRequest();

        try {
            responseString = Objects.requireNonNull(JsonUtils.getJsonFromPOST(request, entityBuilder.build(), 15));
        } catch (Exception ex) {
            timeoutTries++;
            if (timeoutTries < Backend.getOptions().triesBeforeErrorTimeoutOrConnectionFailed.getValue()) {
                LOGGER.warn("DeepL translation request failed! Trying again.. (TIMEOUT OR CONNECTION FAILED)");
                if (!process.running) return null;
                return this._translate(text, timeoutTries, process);
            }
            throw ex;
        }

        LOGGER.info("<-- Response from DeepL: " + responseString);

        // Check if response contains error information
        if (responseString.contains("\"message\"") && responseString.contains("\"detail\"")) {
            LOGGER.error("DeepL API returned an error: " + responseString);
            throw new Exception("DeepL API error: " + responseString);
        }

        DeepLResponse response = gson.fromJson(responseString, DeepLResponse.class);

        if (!process.running) return null;

        if ((response != null) && (response.translations != null) && (response.translations.length > 0)) {
            return response.translations[0].text;
        } else {
            LOGGER.warn("DeepL response was null or empty. Response object: " + (response == null ? "null" : "not null") + 
                       ", translations: " + (response != null && response.translations != null ? response.translations.length : "null"));
        }

        return null;

    }

    /**
     * Converts language codes to DeepL's expected format.
     * DeepL uses uppercase ISO codes with some special cases.
     */
    private String convertToDeepLLanguageCode(String languageCode) {
        if (languageCode == null) return "";
        
        // Convert to uppercase
        String upperCode = languageCode.toUpperCase();
        
        // Handle special cases for DeepL
        switch (upperCode) {
            case "ZH": // Chinese simplified
                return "ZH-HANS";
            case "PT": // Portuguese - DeepL requires PT-PT or PT-BR
                return "PT-PT"; // Default to European Portuguese
            case "EN": // English - DeepL can use EN-US or EN-GB
                return "EN-US"; // Default to American English
            case "ES": // Spanish
                if (upperCode.contains("MX")) return "ES-MX";
                if (upperCode.contains("419")) return "ES-419";
                return "ES"; // Default Spanish
            case "JP": // Japanese - DeepL uses JA
                return "JA";
            default:
                // Handle variants like es-ES, es-MX, etc.
                if (upperCode.contains("-")) {
                    return upperCode;
                }
                return upperCode;
        }
    }

    @Override
    public @NotNull String getRawPrompt() {
        return "";
    }

    @Override
    public @NotNull LanguageType getLanguageType() {
        return LanguageType.ISO;
    }

}
