package de.keksuccino.linguji.linguji.backend.engine.engines.gemini;

import com.google.gson.Gson;
import de.keksuccino.linguji.linguji.backend.Backend;
import de.keksuccino.linguji.linguji.backend.subtitle.translation.TranslationProcess;
import de.keksuccino.linguji.linguji.backend.engine.AbstractTranslationEngine;
import de.keksuccino.linguji.linguji.backend.engine.engines.TranslationEngines;
import de.keksuccino.linguji.linguji.backend.lib.lang.LanguageType;
import de.keksuccino.linguji.linguji.backend.engine.engines.gemini.exceptions.GeminiException;
import de.keksuccino.linguji.linguji.backend.engine.engines.gemini.exceptions.GeminiRequestHardBlockedException;
import de.keksuccino.linguji.linguji.backend.engine.engines.gemini.request.GeminiContent;
import de.keksuccino.linguji.linguji.backend.engine.engines.gemini.request.GeminiGenerateContentRequest;
import de.keksuccino.linguji.linguji.backend.engine.engines.gemini.response.GeminiResponse;
import de.keksuccino.linguji.linguji.backend.engine.engines.gemini.response.GeminiResponseCandidate;
import de.keksuccino.linguji.linguji.backend.engine.engines.gemini.response.GeminiResponseCandidateContentPart;
import de.keksuccino.linguji.linguji.backend.engine.engines.gemini.safety.GeminiSafetySetting;
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

public class GeminiTranslationEngine extends AbstractTranslationEngine {

    private static final SimpleLogger LOGGER = LogHandler.getLogger();
    public static final String GEMINI_API_URL_BASE = "https://generativelanguage.googleapis.com/v1beta/models/";
    public static final String GEMINI_API_URL_SUFFIX = ":generateContent";

    @NotNull
    protected final String apiKey;
    @NotNull
    protected final String prompt;
    @NotNull
    protected final String modelId;

    public GeminiTranslationEngine(@NotNull String apiKey, @NotNull String prompt, @NotNull String modelId, @NotNull Locale sourceLanguage, @NotNull Locale targetLanguage) {
        super(TranslationEngines.GEMINI_PRO, sourceLanguage, targetLanguage);
        this.apiKey = Objects.requireNonNull(apiKey);
        this.prompt = Objects.requireNonNull(prompt);
        this.modelId = Objects.requireNonNull(modelId);
    }

    @Override
    public @Nullable String translate(@NotNull String text, @NotNull TranslationProcess process) throws Exception {
        return this._translate(text, process, new GeminiTriesCounter(), new GeminiSafetyThresholdOverrideContext());
    }

    @Nullable
    protected String _translate(@NotNull String text, @NotNull TranslationProcess process, @NotNull GeminiTriesCounter triesCounter, @NotNull GeminiSafetyThresholdOverrideContext thresholdOverrideContext) throws Exception {

        if (!process.running) return null;

        String responseString;
        Gson gson = new Gson();

        try {
            
            String apiUrl = GEMINI_API_URL_BASE + this.modelId + GEMINI_API_URL_SUFFIX;

            HttpRequest request = HttpRequest.create(apiUrl)
                    .addHeaderEntry("Content-Type", "application/json")
                    .addHeaderEntry("accept", "application/json")
                    .addHeaderEntry("x-goog-api-key", this.apiKey);

            EntityBuilder entityBuilder = EntityBuilder.create();
            entityBuilder.setContentEncoding("UTF-8");
            entityBuilder.setContentType(ContentType.APPLICATION_JSON);

            String promptFinal = this.getFinalPrompt(text);

            GeminiSafetySetting.SafetyThreshold overrideThreshold = (thresholdOverrideContext.overrideHardBlock != null) ? thresholdOverrideContext.overrideHardBlock : thresholdOverrideContext.overrideSoftBlock;
            if ((overrideThreshold != null)) {
                LOGGER.warn("Overriding all Gemini safety thresholds with: " + overrideThreshold.name + " (TRIGGER: " + ((thresholdOverrideContext.overrideSoftBlock != null) ? "SOFT-BLOCK" : "HARD-BLOCK") + ")");
            }

            String json = gson.toJson(
                    GeminiGenerateContentRequest.create()
                            .setSafetySettings(
                                    new GeminiSafetySetting(GeminiSafetySetting.SafetyCategory.HARASSMENT, (overrideThreshold != null) ? overrideThreshold : Objects.requireNonNull(GeminiSafetySetting.SafetyThreshold.getByName(Backend.getOptions().geminiHarmCategoryHarassmentSetting.getValue()))),
                                    new GeminiSafetySetting(GeminiSafetySetting.SafetyCategory.DANGEROUS_CONTENT, (overrideThreshold != null) ? overrideThreshold : Objects.requireNonNull(GeminiSafetySetting.SafetyThreshold.getByName(Backend.getOptions().geminiHarmCategoryDangerousContentSetting.getValue()))),
                                    new GeminiSafetySetting(GeminiSafetySetting.SafetyCategory.SEXUALLY_EXPLICIT, (overrideThreshold != null) ? overrideThreshold : Objects.requireNonNull(GeminiSafetySetting.SafetyThreshold.getByName(Backend.getOptions().geminiHarmCategorySexuallyExplicitSetting.getValue()))),
                                    new GeminiSafetySetting(GeminiSafetySetting.SafetyCategory.HATE_SPEECH, (overrideThreshold != null) ? overrideThreshold : Objects.requireNonNull(GeminiSafetySetting.SafetyThreshold.getByName(Backend.getOptions().geminiHarmCategoryHateSpeechSetting.getValue()))))
                            .setContents(GeminiContent.create().addPart("text", promptFinal))
            );
            Objects.requireNonNull(json);

            LOGGER.info("--> Sending to Gemini: " + json);

            entityBuilder.setText(json);
            entityBuilder.gzipCompressed();

            if (!process.running) return null;

            this.startRequest();

            try {
                responseString = JsonUtils.getJsonFromPOST(request, entityBuilder.build(), 15);
            } catch (Exception ex) {
                triesCounter.timeout++;
                if (triesCounter.timeout < Backend.getOptions().triesBeforeErrorTimeoutOrConnectionFailed.getValue()) {
                    LOGGER.warn("Gemini translation request failed! Trying again.. (TIMEOUT OR CONNECTION FAILED)");
                    if (!process.running) return null;
                    return this._translate(text, process, triesCounter, thresholdOverrideContext);
                }
                throw ex;
            }

            LOGGER.info("<-- Response from Gemini: " + responseString);

        } catch (Exception ex) {
            throw new GeminiException(ex);
        }

        if (!process.running) return null;

        GeminiResponse response = gson.fromJson(responseString, GeminiResponse.class);
        if (response == null) throw new GeminiException("Parsed Gemini response was NULL!");
        if (response.promptFeedback != null) {
            if (response.promptFeedback.blockReason != null) {
                triesCounter.hardBlock++;
                if (triesCounter.hardBlock < Backend.getOptions().geminiTriesBeforeErrorHardBlock.getValue()) {
                    LOGGER.warn("Gemini translation request failed! Trying again.. (HARD-BLOCKED: " + response.promptFeedback.blockReason + ")");
                    if (thresholdOverrideContext.shouldTryOverrideHard(triesCounter.hardBlock)) {
                        thresholdOverrideContext.nextHard();
                    }
                    if (!process.running) return null;
                    return this._translate(text, process, triesCounter, thresholdOverrideContext);
                }
                throw new GeminiRequestHardBlockedException(response.promptFeedback.blockReason.toUpperCase(), responseString);
            }
        }
        if (response.error != null) {
            //TODO better use error code here? is the error code used exclusively for this specific error?
            if ((response.error.message != null) && !response.error.message.contains("location is not supported")) {
                //If error is NOT "user location not supported", try again
                triesCounter.generic++;
                if (triesCounter.generic < Backend.getOptions().triesBeforeErrorGeneric.getValue()) {
                    LOGGER.warn("Gemini translation request failed! Trying again.. (ERROR CODE: " + response.error.code + " | MESSAGE: " + response.error.message + ")");
                    if (!process.running) return null;
                    return this._translate(text, process, triesCounter, thresholdOverrideContext);
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
                    if (triesCounter.softBlock < Backend.getOptions().geminiTriesBeforeErrorSoftBlock.getValue()) {
                        LOGGER.warn("Gemini translation request failed! Trying again.. (SOFT-BLOCKED: " + candidate.finishReason + ")");
                        if (thresholdOverrideContext.shouldTryOverrideSoft(triesCounter.softBlock)) {
                            thresholdOverrideContext.nextSoft();
                        }
                        if (!process.running) return null;
                        return this._translate(text, process, triesCounter, thresholdOverrideContext);
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
    public @NotNull String getRawPrompt() {
        return this.prompt;
    }

    @Override
    public @NotNull LanguageType getLanguageType() {
        return LanguageType.DISPLAY;
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
            if (!Backend.getOptions().geminiOverrideSafetyThresholdSoftBlock.getValue()) return false;
            return (softBlockTries >= Backend.getOptions().geminiOverrideSafetyThresholdSoftBlockAfterTries.getValue());
        }

        protected boolean shouldTryOverrideHard(int hardBlockTries) {
            if (!Backend.getOptions().geminiOverrideSafetyThresholdHardBlock.getValue()) return false;
            return (hardBlockTries >= Backend.getOptions().geminiOverrideSafetyThresholdHardBlockAfterTries.getValue());
        }

        protected void nextSoft() {
            if (this.softMaxedOut) {
                this.resetSoft();
                return;
            }
            if (Backend.getOptions().geminiOverrideSafetyThresholdSoftBlockTriesPerLevel.getValue() <= 0) {
                Backend.getOptions().geminiOverrideSafetyThresholdSoftBlockTriesPerLevel.setValue(1);
            }
            if ((this.overrideSoftBlock == null) || (this.softBlockPerLevelTries >= Backend.getOptions().geminiOverrideSafetyThresholdSoftBlockTriesPerLevel.getValue())) {
                if (!Backend.getOptions().geminiOverrideSafetyThresholdSkipLowLevels.getValue()) {
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
                } else {
                    if (this.overrideSoftBlock != GeminiSafetySetting.SafetyThreshold.BLOCK_NONE) {
                        this.overrideSoftBlock = GeminiSafetySetting.SafetyThreshold.BLOCK_NONE;
                        this.softBlockPerLevelTries = 0;
                    }
                }
            }
            //If still >= at this point, consider it maxed-out because otherwise it would've been reset earlier
            if (this.softBlockPerLevelTries >= Backend.getOptions().geminiOverrideSafetyThresholdSoftBlockTriesPerLevel.getValue()) {
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
            if (Backend.getOptions().geminiOverrideSafetyThresholdHardBlockTriesPerLevel.getValue() <= 0) {
                Backend.getOptions().geminiOverrideSafetyThresholdHardBlockTriesPerLevel.setValue(1);
            }
            if ((this.overrideHardBlock == null) || (this.hardBlockPerLevelTries >= Backend.getOptions().geminiOverrideSafetyThresholdHardBlockTriesPerLevel.getValue())) {
                if (!Backend.getOptions().geminiOverrideSafetyThresholdSkipLowLevels.getValue()) {
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
                } else {
                    if (this.overrideHardBlock != GeminiSafetySetting.SafetyThreshold.BLOCK_NONE) {
                        this.overrideHardBlock = GeminiSafetySetting.SafetyThreshold.BLOCK_NONE;
                        this.hardBlockPerLevelTries = 0;
                    }
                }
            }
            //If still >= at this point, consider it maxed-out because otherwise it would've been reset earlier
            if (this.hardBlockPerLevelTries >= Backend.getOptions().geminiOverrideSafetyThresholdHardBlockTriesPerLevel.getValue()) {
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
