package de.keksuccino.subtitleai.ai.gemini;

import com.google.gson.*;
import de.keksuccino.subtitleai.ai.AiTranslator;
import de.keksuccino.subtitleai.ai.gemini.exceptions.GeminiException;
import de.keksuccino.subtitleai.ai.gemini.exceptions.GeminiRequestBlockedException;
import de.keksuccino.subtitleai.util.HttpRequest;
import de.keksuccino.subtitleai.util.JsonUtils;
import de.keksuccino.subtitleai.util.ThreadUtils;
import org.apache.hc.client5.http.entity.EntityBuilder;
import org.apache.hc.core5.http.ContentType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import java.util.Objects;

public class GeminiTranslator implements AiTranslator {

    private static final Logger LOGGER = LogManager.getLogger();

    public static final String GEMINI_API_URL = "https://generativelanguage.googleapis.com/v1/models/gemini-pro:generateContent";
    public static final int MAX_CHARACTERS = 1500;
    public static final int MAX_TRIES_BEFORE_ERROR_SOFT_BLOCK = 2000;
    public static final int MAX_TRIES_BEFORE_ERROR_HARD_BLOCK = 10;
    public static final int MAX_TRIES_BEFORE_ERROR_GENERIC = 2000;
    public static final long WAIT_MS_AFTER_FAILED_TRY = 3000;

    @NotNull
    protected final String apiKey;
    @NotNull
    protected final String prompt;

    public GeminiTranslator(@NotNull String apiKey) {
        this.apiKey = Objects.requireNonNull(apiKey);
        this.prompt = DEFAULT_PROMPT;
    }

    public GeminiTranslator(@NotNull String apiKey, @NotNull String prompt) {
        this.apiKey = Objects.requireNonNull(apiKey);
        this.prompt = Objects.requireNonNull(prompt);
    }

    @NotNull
    public String translate(@NotNull String text, @NotNull String sourceLanguage, @NotNull String targetLanguage) throws Exception {
        return this._translate(text, sourceLanguage, targetLanguage, 0);
    }

    @NotNull
    protected String _translate(@NotNull String text, @NotNull String sourceLanguage, @NotNull String targetLanguage, int failedTries) throws Exception {

        String responseString;
        Gson gson = new Gson();

        try {

//            text = this.replaceProfanity(text, "*");

            HttpRequest request = HttpRequest.create(GEMINI_API_URL)
                    .addHeaderEntry("Content-Type", "application/json")
                    .addHeaderEntry("accept", "application/json")
                    .addHeaderEntry("x-goog-api-key", this.apiKey);

            EntityBuilder entityBuilder = EntityBuilder.create();
            entityBuilder.setContentEncoding("UTF-8");
            entityBuilder.setContentType(ContentType.APPLICATION_JSON);

            String promptFinal = this.getFinalPrompt(text, sourceLanguage, targetLanguage);

            String json = gson.toJson(
                    GeminiGenerateContentRequest.create()
                            .setSafetySettings(
                                    new GeminiSafetySetting(GeminiSafetySetting.SafetyCategory.HARASSMENT, GeminiSafetySetting.SafetyThreshold.BLOCK_ONLY_HIGH),
                                    new GeminiSafetySetting(GeminiSafetySetting.SafetyCategory.DANGEROUS_CONTENT, GeminiSafetySetting.SafetyThreshold.BLOCK_ONLY_HIGH),
                                    new GeminiSafetySetting(GeminiSafetySetting.SafetyCategory.SEXUALLY_EXPLICIT, GeminiSafetySetting.SafetyThreshold.BLOCK_ONLY_HIGH),
                                    new GeminiSafetySetting(GeminiSafetySetting.SafetyCategory.HATE_SPEECH, GeminiSafetySetting.SafetyThreshold.BLOCK_ONLY_HIGH))
                            .setContents(GeminiContent.create().addPart("text", promptFinal))
            );
            Objects.requireNonNull(json);

            LOGGER.info("########### SENDING TO GEMINI: " + json);

            entityBuilder.setText(json);
            entityBuilder.gzipCompressed();

            responseString = JsonUtils.getJsonFromPOST(request, entityBuilder.build());

            LOGGER.info("########### RESPONSE FROM GEMINI: " + responseString);

        } catch (Exception ex) {
            throw new GeminiException(ex);
        }

        if (responseString != null) {

            GeminiResponse response = gson.fromJson(responseString, GeminiResponse.class);
            if (response == null) throw new GeminiException("Parsed response was NULL!");
            if (response.promptFeedback != null) {
                if (response.promptFeedback.blockReason != null) {
                    failedTries++;
                    if (failedTries <= MAX_TRIES_BEFORE_ERROR_HARD_BLOCK) {
                        LOGGER.info("########### REQUEST FAILED! TRYING AGAIN! (HARD-BLOCKED: " + response.promptFeedback.blockReason + ")");
                        ThreadUtils.sleep(WAIT_MS_AFTER_FAILED_TRY);
                        return this._translate(text, sourceLanguage, targetLanguage, failedTries);
                    }
                    throw new GeminiRequestBlockedException(response.promptFeedback.blockReason.toUpperCase(), responseString);
                }
            }
            if (response.error != null) {
                if ((response.error.message != null) && !response.error.message.contains("location is not supported")) {
                    //If error is NOT "user location not supported", try again
                    failedTries++;
                    if (failedTries <= MAX_TRIES_BEFORE_ERROR_GENERIC) {
                        LOGGER.info("########### REQUEST FAILED! TRYING AGAIN! (ERROR CODE: " + response.error.code + " | MESSAGE: " + response.error.message + ")");
                        ThreadUtils.sleep(WAIT_MS_AFTER_FAILED_TRY);
                        return this._translate(text, sourceLanguage, targetLanguage, failedTries);
                    }
                }
                throw new GeminiException("Error Code: " + response.error.code + " | Status: " + response.error.status + " | Message: " + response.error.message);
            } else {
                GeminiResponseCandidate candidate = null;
                GeminiResponseCandidateContentPart part = null;
                try {
                    candidate = response.candidates[0];
                } catch (Exception ignore) {}
                if (candidate != null) {
                    if (!"STOP".equalsIgnoreCase(candidate.finishReason)) {
                        failedTries++;
                        if (failedTries <= MAX_TRIES_BEFORE_ERROR_SOFT_BLOCK) {
                            LOGGER.info("########### REQUEST FAILED! TRYING AGAIN! (SOFT-BLOCKED: " + candidate.finishReason + ")");
                            ThreadUtils.sleep(WAIT_MS_AFTER_FAILED_TRY);
                            return this._translate(text, sourceLanguage, targetLanguage, failedTries);
                        }
                        throw new GeminiException("Gemini request soft-blocked! Finish reason: " + candidate.finishReason + " | Full Response: " + responseString);
                    }
                }
                try {
                    if (candidate != null) part = candidate.content.parts[0];
                } catch (Exception ignore) {};
                if (part != null) {
                    if (part.text == null) throw new GeminiException("Text of response was NULL! Response: " + responseString);
                    return part.text;
                } else {
                    throw new GeminiException("Failed to parse response! Response: " + responseString);
                }
            }

        } else {
            throw new GeminiException("Response was NULL!");
        }

    }

//    @NotNull
//    protected String _translate(@NotNull String text, @NotNull String sourceLanguage, @NotNull String targetLanguage, int failedTries) throws GeminiException, GeminiRequestBlockedException {
//
//        String responseString;
//        Gson gson = new Gson();
//
//        try {
//
//            text = this.replaceProfanity(text, "*");
//
//            HttpRequest request = HttpRequest.create(GEMINI_API_URL)
//                    .addHeaderEntry("Content-Type", "application/json")
//                    .addHeaderEntry("accept", "application/json")
//                    .addHeaderEntry("x-goog-api-key", this.apiKey);
//
//            EntityBuilder entityBuilder = EntityBuilder.create();
//            entityBuilder.setContentEncoding("UTF-8");
//            entityBuilder.setContentType(ContentType.APPLICATION_JSON);
//
//            String promptFinal = this.getFinalPrompt(text, sourceLanguage, targetLanguage);
//
//            JsonObject json = new JsonObject();
//            json.add("contents", gson.toJsonTree(GeminiRequest.create().addPart("text", promptFinal)));
//
//            LOGGER.info("########### SENDING TO GEMINI: " + json);
//
//            entityBuilder.setText(json.toString());
//            entityBuilder.gzipCompressed();
//
//            responseString = JsonUtils.getJsonFromPOST(request, entityBuilder.build());
//
//            LOGGER.info("########### RESPONSE FROM GEMINI: " + responseString);
//
//        } catch (Exception ex) {
//            throw new GeminiException(ex);
//        }
//
//        if (responseString != null) {
//
//            GeminiResponse response = gson.fromJson(responseString, GeminiResponse.class);
//            if (response == null) throw new GeminiException("Parsed response was NULL!");
//            if (response.promptFeedback != null) {
//                if (response.promptFeedback.blockReason != null) {
//                    throw new GeminiRequestBlockedException(response.promptFeedback.blockReason.toUpperCase(), responseString);
//                }
//            }
//            if (response.error != null) {
//                if ((response.error.message != null) && !response.error.message.contains("location is not supported")) {
//                    //If error is NOT "user location not supported", try again
//                    failedTries++;
//                    if (failedTries <= MAX_TRIES_BEFORE_ERROR) {
//                        LOGGER.info("########### REQUEST FAILED! TRYING AGAIN! (some error happened)");
//                        return this._translate(text, sourceLanguage, targetLanguage, failedTries);
//                    }
//                }
//                throw new GeminiException("Error! Code: " + response.error.code + " | Status: " + response.error.status + " | Message: " + response.error.message);
//            } else {
//                GeminiResponseCandidate candidate = null;
//                GeminiResponseCandidateContentPart part = null;
//                try {
//                    candidate = response.candidates[0];
//                } catch (Exception ignore) {}
//                if (candidate != null) {
//                    if (candidate.finishReason.equalsIgnoreCase("SAFETY")) {
//                        //If request did not get blocked, but failed because of "SAFETY", try again
//                        failedTries++;
//                        if (failedTries <= MAX_TRIES_BEFORE_ERROR) {
//                            LOGGER.info("########### REQUEST FAILED! TRYING AGAIN! (soft-block SAFETY)");
//                            return this._translate(text, sourceLanguage, targetLanguage, failedTries);
//                        }
//                        throw new GeminiException("Gemini request failed! Finish reason: " + candidate.finishReason + " | Full Response: " + responseString);
//                    } else if (!candidate.finishReason.equalsIgnoreCase("STOP")) {
//                        throw new GeminiException("Gemini request failed! Finish reason: " + candidate.finishReason + " | Full Response: " + responseString);
//                    }
//                }
//                try {
//                    if (candidate != null) part = candidate.content.parts[0];
//                } catch (Exception ignore) {};
//                if (part != null) {
//                    if (part.text == null) throw new GeminiException("Text of response was NULL! Response: " + responseString);
//                    return part.text;
//                } else {
//                    throw new GeminiException("Failed to parse response! Response: " + responseString);
//                }
//            }
//
//        } else {
//            throw new GeminiException("Response was NULL!");
//        }
//
//    }

    @Override
    public int getMaxCharacterLength() {
        return MAX_CHARACTERS;
    }

    @Override
    public @NotNull String getRawPrompt() {
        return this.prompt;
    }

}
