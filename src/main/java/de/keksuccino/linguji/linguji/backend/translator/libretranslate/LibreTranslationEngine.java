package de.keksuccino.linguji.linguji.backend.translator.libretranslate;

import com.google.gson.Gson;
import de.keksuccino.linguji.linguji.backend.Backend;
import de.keksuccino.linguji.linguji.backend.subtitle.translation.TranslationProcess;
import de.keksuccino.linguji.linguji.backend.translator.AbstractTranslationEngine;
import de.keksuccino.linguji.linguji.backend.translator.TranslationEngines;
import de.keksuccino.linguji.linguji.backend.lib.lang.LanguageType;
import de.keksuccino.linguji.linguji.backend.translator.libretranslate.response.LibreTranslateResponse;
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

// Libre Translate Servers: https://github.com/LibreTranslate/LibreTranslate?tab=readme-ov-file#mirrors

public class LibreTranslationEngine extends AbstractTranslationEngine {

    private static final SimpleLogger LOGGER = LogHandler.getLogger();

    @Nullable
    public final String apiKey;
    @NotNull
    public final String apiUrl;

    public LibreTranslationEngine(@NotNull String apiUrl, @Nullable String apiKey, @NotNull Locale sourceLanguage, @NotNull Locale targetLanguage) {
        super(TranslationEngines.LIBRE_TRANSLATE, sourceLanguage, targetLanguage);
        this.apiKey = apiKey;
        this.apiUrl = apiUrl;
    }

    @Override
    public @Nullable String translate(@NotNull String text, @NotNull TranslationProcess process) throws Exception {
        return this._translate(text, 0, process);
    }

    protected @Nullable String _translate(@NotNull String text, int timeoutTries, @NotNull TranslationProcess process) throws Exception {

        if (!process.running) return null;

        Gson gson = new Gson();

        HttpRequest httpRequest = HttpRequest.create(this.apiUrl)
                .addHeaderEntry("Content-Type", "application/json");

        EntityBuilder entityBuilder = EntityBuilder.create();
        entityBuilder.setContentEncoding("UTF-8");
        entityBuilder.setContentType(ContentType.APPLICATION_JSON);

        LibreTranslateRequest request = new LibreTranslateRequest();
        request.api_key = this.apiKey;
        request.q = text;
        request.source = this.getSourceLanguageString();
        request.target = this.getTargetLanguageString();

        String json = Objects.requireNonNull(gson.toJson(request));

        LOGGER.info("--> Sending to Libre Translate: " + json);

        entityBuilder.setText(json);

        if (!process.running) return null;

        String responseString;

        this.startRequest();

        try {
            responseString = Objects.requireNonNull(JsonUtils.getJsonFromPOST(httpRequest, entityBuilder.build(), 15));
        } catch (Exception ex) {
            timeoutTries++;
            if (timeoutTries < Backend.getOptions().triesBeforeErrorTimeoutOrConnectionFailed.getValue()) {
                LOGGER.warn("Libre Translate translation request failed! Trying again.. (TIMEOUT OR CONNECTION FAILED)");
                if (!process.running) return null;
                return this._translate(text, timeoutTries, process);
            }
            throw ex;
        }

        LOGGER.info("<-- Response from Libre Translate: " + responseString);

        LibreTranslateResponse response = Objects.requireNonNull(gson.fromJson(responseString, LibreTranslateResponse.class));

        return response.translatedText;

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
