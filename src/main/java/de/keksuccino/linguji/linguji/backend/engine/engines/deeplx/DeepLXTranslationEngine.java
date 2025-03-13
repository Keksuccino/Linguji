package de.keksuccino.linguji.linguji.backend.engine.engines.deeplx;

import com.google.gson.Gson;
import de.keksuccino.linguji.linguji.backend.Backend;
import de.keksuccino.linguji.linguji.backend.subtitle.translation.TranslationProcess;
import de.keksuccino.linguji.linguji.backend.engine.AbstractTranslationEngine;
import de.keksuccino.linguji.linguji.backend.engine.engines.TranslationEngines;
import de.keksuccino.linguji.linguji.backend.lib.HttpRequest;
import de.keksuccino.linguji.linguji.backend.lib.JsonUtils;
import de.keksuccino.linguji.linguji.backend.lib.lang.LanguageType;
import de.keksuccino.linguji.linguji.backend.lib.lang.Locale;
import de.keksuccino.linguji.linguji.backend.lib.logger.LogHandler;
import de.keksuccino.linguji.linguji.backend.lib.logger.SimpleLogger;
import org.apache.hc.client5.http.entity.EntityBuilder;
import org.apache.hc.core5.http.ContentType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.util.Objects;

public class DeepLXTranslationEngine extends AbstractTranslationEngine {

    private static final SimpleLogger LOGGER = LogHandler.getLogger();

    public DeepLXTranslationEngine(@NotNull Locale sourceLanguage, @NotNull Locale targetLanguage) {
        super(TranslationEngines.DEEPLX, sourceLanguage, targetLanguage);
    }

    @Override
    public @Nullable String translate(@NotNull String text, @NotNull TranslationProcess process) throws Exception {
        return this._translate(text, 0, 0, process);
    }

    protected @Nullable String _translate(@NotNull String text, int timeoutTries, int emptyResponseTries, @NotNull TranslationProcess process) throws Exception {

        if (!process.running) return null;

        Gson gson = new Gson();

        HttpRequest request = HttpRequest.create(Backend.getOptions().deepLxUrl.getValue())
                .addHeaderEntry("Content-Type", "application/json");

        EntityBuilder entityBuilder = EntityBuilder.create();
        entityBuilder.setContentEncoding("UTF-8");
        entityBuilder.setContentType(ContentType.APPLICATION_JSON);

        String json = Objects.requireNonNull(gson.toJson(new DeepLXRequest(text, this.getSourceLanguageString(), this.getTargetLanguageString())));

        LOGGER.info("--> Sending to DeepLX: " + json);

        entityBuilder.setText(json);

        if (!process.running) return null;

        String responseString;

        this.startRequest();

        try {
            responseString = Objects.requireNonNull(JsonUtils.getJsonFromPOST(request, entityBuilder.build(), 15));
        } catch (Exception ex) {
            timeoutTries++;
            if (timeoutTries < Backend.getOptions().triesBeforeErrorTimeoutOrConnectionFailed.getValue()) {
                LOGGER.warn("DeepLX translation request timed out! Trying again..");
                if (!process.running) return null;
                return this._translate(text, timeoutTries, emptyResponseTries, process);
            }
            throw ex;
        }

        LOGGER.info("<-- Response from DeepLX: " + responseString);

        DeepLXResponse response = Objects.requireNonNull(gson.fromJson(responseString, DeepLXResponse.class));

        if ((response.getText() == null) || (response.getText().isEmpty())) {
            emptyResponseTries++;
            if (emptyResponseTries < Backend.getOptions().deepLxTriesBeforeErrorEmptyResponse.getValue()) {
                LOGGER.warn("DeepLX response was empty! Trying again..");
                if (!process.running) return null;
                return this._translate(text, timeoutTries, emptyResponseTries, process);
            }
            throw new IllegalStateException("DeepLX response was empty!");
        }

        if (response.code != 200) throw new IllegalStateException("DeepLX returned error code: " + response.code + " (FULL RESPONSE: " + responseString + ")");

        if (!process.running) return null;

        return response.getText();

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
