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
 * Untested, but should work in theory.
 */
public class DeepLTranslationEngine extends AbstractTranslationEngine {

    private static final SimpleLogger LOGGER = LogHandler.getLogger();

    public static final String DEEPL_URL = "https://api-free.deepl.com/v2/translate";

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

        HttpRequest request = HttpRequest.create(DEEPL_URL)
                .addHeaderEntry("Content-Type", "application/json")
                .addHeaderEntry("Authorization", "DeepL-Auth-Key " + this.apiKey);

        EntityBuilder entityBuilder = EntityBuilder.create();
        entityBuilder.setContentEncoding("UTF-8");
        entityBuilder.setContentType(ContentType.APPLICATION_JSON);

        String json = Objects.requireNonNull(gson.toJson(new DeepLRequest(this.getTargetLanguageString(), text)));

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

        DeepLResponse response = gson.fromJson(responseString, DeepLResponse.class);

        if (!process.running) return null;

        if ((response.translations != null) && (response.translations.length > 0)) {
            return response.translations[0].text;
        }

        return null;

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
