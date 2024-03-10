package de.keksuccino.subtitleai.ai.gemini;

import com.google.gson.*;
import de.keksuccino.subtitleai.Main;
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
import org.jetbrains.annotations.Nullable;
import java.util.Objects;

public class GeminiTranslator implements AiTranslator {

    private static final Logger LOGGER = LogManager.getLogger();
    public static final String GEMINI_API_URL = "https://generativelanguage.googleapis.com/v1/models/gemini-pro:generateContent";

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
        return this._translate(text, sourceLanguage, targetLanguage, new GeminiTriesCounter(), new GeminiSafetyThresholdOverrideContext());
    }

    @NotNull
    protected String _translate(@NotNull String text, @NotNull String sourceLanguage, @NotNull String targetLanguage, @NotNull GeminiTriesCounter triesCounter, @NotNull GeminiSafetyThresholdOverrideContext thresholdOverrideContext) throws Exception {

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

            GeminiSafetySetting.SafetyThreshold overrideThreshold = (thresholdOverrideContext.overrideHardBlock != null) ? thresholdOverrideContext.overrideHardBlock : thresholdOverrideContext.overrideSoftBlock;
            if (overrideThreshold != null) {
                LOGGER.info("Overriding all Gemini safety thresholds with: " + overrideThreshold.name + " (TRIGGER: " + ((thresholdOverrideContext.overrideSoftBlock != null) ? "SOFT-BLOCK" : "HARD-BLOCK") + ")");
            }

            String json = gson.toJson(
                    GeminiGenerateContentRequest.create()
                            .setSafetySettings(
                                    new GeminiSafetySetting(GeminiSafetySetting.SafetyCategory.HARASSMENT, (overrideThreshold != null) ? overrideThreshold : Objects.requireNonNull(GeminiSafetySetting.SafetyThreshold.getByName(Main.getOptions().geminiHarmCategoryHarassmentSetting.getValue()))),
                                    new GeminiSafetySetting(GeminiSafetySetting.SafetyCategory.DANGEROUS_CONTENT, (overrideThreshold != null) ? overrideThreshold : Objects.requireNonNull(GeminiSafetySetting.SafetyThreshold.getByName(Main.getOptions().geminiHarmCategoryDangerousContentSetting.getValue()))),
                                    new GeminiSafetySetting(GeminiSafetySetting.SafetyCategory.SEXUALLY_EXPLICIT, (overrideThreshold != null) ? overrideThreshold : Objects.requireNonNull(GeminiSafetySetting.SafetyThreshold.getByName(Main.getOptions().geminiHarmCategorySexuallyExplicitSetting.getValue()))),
                                    new GeminiSafetySetting(GeminiSafetySetting.SafetyCategory.HATE_SPEECH, (overrideThreshold != null) ? overrideThreshold : Objects.requireNonNull(GeminiSafetySetting.SafetyThreshold.getByName(Main.getOptions().geminiHarmCategoryHateSpeechSetting.getValue()))))
                            .setContents(GeminiContent.create().addPart("text", promptFinal))
            );
            Objects.requireNonNull(json);

            LOGGER.info("--> Sending to Gemini: " + json);

            entityBuilder.setText(json);
            entityBuilder.gzipCompressed();

            try {
                responseString = JsonUtils.getJsonFromPOST(request, entityBuilder.build());
            } catch (Exception ex) {
                triesCounter.timeout++;
                if (triesCounter.timeout <= Main.getOptions().geminiTriesBeforeErrorTimeoutOrConnectionFailed.getValue()) {
                    thresholdOverrideContext.reset();
                    LOGGER.info("Gemini translation request failed! Trying again.. (TIMEOUT OR CONNECTION FAILED)");
                    ThreadUtils.sleep(Main.getOptions().waitMillisBeforeNextTry.getValue());
                    return this._translate(text, sourceLanguage, targetLanguage, triesCounter, thresholdOverrideContext);
                }
                throw ex;
            }

            LOGGER.info("<-- Response from Gemini: " + responseString);

        } catch (Exception ex) {
            throw new GeminiException(ex);
        }

        GeminiResponse response = gson.fromJson(responseString, GeminiResponse.class);
        if (response == null) throw new GeminiException("Parsed Gemini response was NULL!");
        if (response.promptFeedback != null) {
            if (response.promptFeedback.blockReason != null) {
                triesCounter.hardBlock++;
                if (triesCounter.hardBlock <= Main.getOptions().geminiTriesBeforeErrorHardBlock.getValue()) {
                    thresholdOverrideContext.resetSoft();
                    LOGGER.info("Gemini translation request failed! Trying again.. (HARD-BLOCKED: " + response.promptFeedback.blockReason + ")");
                    if (thresholdOverrideContext.shouldTryOverrideHard(triesCounter.hardBlock)) {
                        thresholdOverrideContext.nextHard();
                    }
                    ThreadUtils.sleep(Main.getOptions().waitMillisBeforeNextTry.getValue());
                    return this._translate(text, sourceLanguage, targetLanguage, triesCounter, thresholdOverrideContext);
                }
                throw new GeminiRequestBlockedException(response.promptFeedback.blockReason.toUpperCase(), responseString);
            }
        }
        if (response.error != null) {
            if ((response.error.message != null) && !response.error.message.contains("location is not supported")) {
                //If error is NOT "user location not supported", try again
                triesCounter.generic++;
                if (triesCounter.generic <= Main.getOptions().geminiTriesBeforeErrorGeneric.getValue()) {
                    thresholdOverrideContext.reset();
                    LOGGER.info("Gemini translation request failed! Trying again.. (ERROR CODE: " + response.error.code + " | MESSAGE: " + response.error.message + ")");
                    ThreadUtils.sleep(Main.getOptions().waitMillisBeforeNextTry.getValue());
                    return this._translate(text, sourceLanguage, targetLanguage, triesCounter, thresholdOverrideContext);
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
                    triesCounter.softBlock++;
                    if (triesCounter.softBlock <= Main.getOptions().geminiTriesBeforeErrorSoftBlock.getValue()) {
                        thresholdOverrideContext.resetHard();
                        LOGGER.info("Gemini translation request failed! Trying again.. (SOFT-BLOCKED: " + candidate.finishReason + ")");
                        if (thresholdOverrideContext.shouldTryOverrideSoft(triesCounter.softBlock)) {
                            thresholdOverrideContext.nextSoft();
                        }
                        ThreadUtils.sleep(Main.getOptions().waitMillisBeforeNextTry.getValue());
                        return this._translate(text, sourceLanguage, targetLanguage, triesCounter, thresholdOverrideContext);
                    }
                    throw new GeminiException("Gemini translation request soft-blocked! Finish reason: " + candidate.finishReason + " | Full Response: " + responseString);
                }
            }
            try {
                if (candidate != null) part = candidate.content.parts[0];
            } catch (Exception ignore) {};
            if (part != null) {
                if (part.text == null) throw new GeminiException("Text of Gemini response was NULL! Response: " + responseString);
                return part.text;
            } else {
                throw new GeminiException("Failed to parse Gemini response! Response: " + responseString);
            }
        }

    }

    @Override
    public int getMaxCharacterLength() {
        return 1500;
    }

    @Override
    public @NotNull String getRawPrompt() {
        return this.prompt;
    }

    protected static class GeminiTriesCounter {

        protected int timeout = 0;
        protected int softBlock = 0;
        protected int hardBlock = 0;
        protected int generic = 0;

    }

    protected static class GeminiSafetyThresholdOverrideContext {

        @Nullable
        protected GeminiSafetySetting.SafetyThreshold overrideSoftBlock = null;
        @Nullable
        protected GeminiSafetySetting.SafetyThreshold overrideHardBlock = null;
        protected int softBlockPerLevelTries = 0;
        protected int hardBlockPerLevelTries = 0;
        protected boolean softMaxedOut = false;
        protected boolean hardMaxedOut = false;

        protected boolean shouldTryOverrideSoft(int softBlockTries) {
            return (softBlockTries >= Main.getOptions().geminiOverrideSafetyThresholdSoftBlockAfterTries.getValue());
        }

        protected boolean shouldTryOverrideHard(int hardBlockTries) {
            return (hardBlockTries >= Main.getOptions().geminiOverrideSafetyThresholdHardBlockAfterTries.getValue());
        }

        protected void nextSoft() {
            if (this.softMaxedOut) {
                this.resetSoft();
                return;
            }
            if ((this.overrideSoftBlock == null) || (this.softBlockPerLevelTries >= Main.getOptions().geminiOverrideSafetyThresholdSoftBlockTriesPerLevel.getValue())) {
                if (this.overrideSoftBlock == null) {
                    this.overrideSoftBlock = GeminiSafetySetting.SafetyThreshold.BLOCK_LOW_AND_ABOVE;
                    this.softBlockPerLevelTries = 0;
                } else if (this.overrideSoftBlock == GeminiSafetySetting.SafetyThreshold.BLOCK_LOW_AND_ABOVE) {
                    this.overrideSoftBlock = GeminiSafetySetting.SafetyThreshold.BLOCK_MEDIUM_AND_ABOVE;
                    this.softBlockPerLevelTries = 0;
                } else if (this.overrideSoftBlock == GeminiSafetySetting.SafetyThreshold.BLOCK_MEDIUM_AND_ABOVE) {
                    this.overrideSoftBlock = GeminiSafetySetting.SafetyThreshold.BLOCK_ONLY_HIGH;
                    this.softBlockPerLevelTries = 0;
                } else if (this.overrideSoftBlock == GeminiSafetySetting.SafetyThreshold.BLOCK_ONLY_HIGH) {
                    this.overrideSoftBlock = GeminiSafetySetting.SafetyThreshold.BLOCK_NONE;
                    this.softBlockPerLevelTries = 0;
                }
            }
            if ((this.overrideSoftBlock == GeminiSafetySetting.SafetyThreshold.BLOCK_NONE) && (this.softBlockPerLevelTries > 0)) {
                this.softMaxedOut = true;
                this.resetSoft();
            }
            this.softBlockPerLevelTries++;
        }

        protected void nextHard() {
            if (this.hardMaxedOut) {
                this.resetHard();
                return;
            }
            if ((this.overrideHardBlock == null) || (this.hardBlockPerLevelTries >= Main.getOptions().geminiOverrideSafetyThresholdHardBlockTriesPerLevel.getValue())) {
                if (this.overrideHardBlock == null) {
                    this.overrideHardBlock = GeminiSafetySetting.SafetyThreshold.BLOCK_LOW_AND_ABOVE;
                    this.hardBlockPerLevelTries = 0;
                } else if (this.overrideHardBlock == GeminiSafetySetting.SafetyThreshold.BLOCK_LOW_AND_ABOVE) {
                    this.overrideHardBlock = GeminiSafetySetting.SafetyThreshold.BLOCK_MEDIUM_AND_ABOVE;
                    this.hardBlockPerLevelTries = 0;
                } else if (this.overrideHardBlock == GeminiSafetySetting.SafetyThreshold.BLOCK_MEDIUM_AND_ABOVE) {
                    this.overrideHardBlock = GeminiSafetySetting.SafetyThreshold.BLOCK_ONLY_HIGH;
                    this.hardBlockPerLevelTries = 0;
                } else if (this.overrideHardBlock == GeminiSafetySetting.SafetyThreshold.BLOCK_ONLY_HIGH) {
                    this.overrideHardBlock = GeminiSafetySetting.SafetyThreshold.BLOCK_NONE;
                    this.hardBlockPerLevelTries = 0;
                }
            }
            if ((this.overrideHardBlock == GeminiSafetySetting.SafetyThreshold.BLOCK_NONE) && (this.hardBlockPerLevelTries > 0)) {
                this.hardMaxedOut = true;
                this.resetHard();
            }
            this.hardBlockPerLevelTries++;
        }

        protected void resetSoft() {
            this.overrideSoftBlock = null;
            this.softBlockPerLevelTries = 0;
        }

        protected void resetHard() {
            this.overrideHardBlock = null;
            this.hardBlockPerLevelTries = 0;
        }

        protected void reset() {
            this.resetSoft();
            this.resetHard();
        }

    }

}
