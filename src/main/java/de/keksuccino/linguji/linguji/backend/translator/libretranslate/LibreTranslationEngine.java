package de.keksuccino.linguji.linguji.backend.translator.libretranslate;

import com.google.gson.Gson;
import de.keksuccino.linguji.linguji.backend.subtitle.translation.TranslationProcess;
import de.keksuccino.linguji.linguji.backend.translator.ITranslationEngine;
import de.keksuccino.linguji.linguji.backend.translator.libretranslate.response.LibreTranslateResponse;
import de.keksuccino.linguji.linguji.backend.util.HttpRequest;
import de.keksuccino.linguji.linguji.backend.util.JsonUtils;
import de.keksuccino.linguji.linguji.backend.util.logger.LogHandler;
import de.keksuccino.linguji.linguji.backend.util.logger.SimpleLogger;
import org.apache.hc.client5.http.entity.EntityBuilder;
import org.apache.hc.core5.http.ContentType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

// LibreTranslate Servers: https://github.com/LibreTranslate/LibreTranslate?tab=readme-ov-file#mirrors

public class LibreTranslationEngine implements ITranslationEngine {

    private static final SimpleLogger LOGGER = LogHandler.getLogger();

    @Nullable
    public final String apiKey;
    @NotNull
    public final String apiUrl;

    public LibreTranslationEngine(@NotNull String apiUrl, @Nullable String apiKey) {
        this.apiKey = apiKey;
        this.apiUrl = apiUrl;
    }

    @Override
    public @Nullable String translate(@NotNull String text, @NotNull String sourceLanguage, @NotNull String targetLanguage, @NotNull TranslationProcess process) throws Exception {

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
        request.source = sourceLanguage;
        request.target = targetLanguage;

        String json = Objects.requireNonNull(gson.toJson(request));

        LOGGER.info("--> Sending to LibreTranslate: " + json);

        entityBuilder.setText(json);

        if (!process.running) return null;

        String responseString = Objects.requireNonNull(JsonUtils.getJsonFromPOST(httpRequest, entityBuilder.build()));

        LOGGER.info("<-- Response from LibreTranslate: " + responseString);

        LibreTranslateResponse response = Objects.requireNonNull(gson.fromJson(responseString, LibreTranslateResponse.class));

        return response.translatedText;

    }

    @Override
    public @NotNull String getEngineName() {
        return "Libre Translate";
    }

    @Override
    public int getMaxCharacterLength() {
        return 10000000;
    }

    @Override
    public @NotNull String getRawPrompt() {
        return "";
    }

}