package de.keksuccino.polyglot.polyglot.frontend;

import de.keksuccino.polyglot.polyglot.backend.Backend;
import de.keksuccino.polyglot.polyglot.backend.subtitle.translation.TranslationProcess;
import de.keksuccino.polyglot.polyglot.backend.translator.gemini.safety.GeminiSafetySetting;
import de.keksuccino.polyglot.polyglot.backend.util.logger.LogHandler;
import de.keksuccino.polyglot.polyglot.backend.util.logger.SimpleLogger;
import de.keksuccino.polyglot.polyglot.backend.util.options.AbstractOptions;
import de.keksuccino.polyglot.polyglot.frontend.util.SpinnerUtils;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.util.StringConverter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.io.File;

public class MainViewController {

    private static final SimpleLogger LOGGER = LogHandler.getLogger();

    @FXML
    private ProgressBar subtitleProgressBar;
    @FXML
    private ProgressBar totalProgressBar;
    @FXML
    private Button startTranslationButton;
    @FXML
    private Button chooseInputDirButton;
    @FXML
    private Button chooseOutputDirButton;
    @FXML
    private Label inputDirLabel;
    @FXML
    private Label outputDirLabel;
    @FXML
    private TextField sourceLangTextField;
    @FXML
    private TextField targetLangTextField;
    @FXML
    private TextField geminiApiKeyTextField;
    @FXML
    private TextField promptTextField;
    @FXML
    private Spinner<Integer> linesPerPacketSpinner;
    @FXML
    private Spinner<Integer> triesBeforeStopInvalidLineCountSpinner;
    @FXML
    private Spinner<Integer> triesBeforeStopTimeoutSpinner;
    @FXML
    private Spinner<Integer> triesBeforeStopGenericErrorSpinner;
    @FXML
    private Spinner<Integer> triesBeforeStopGeminiSoftBlockSpinner;
    @FXML
    private Spinner<Integer> triesBeforeStopGeminiHardBlockSpinner;
    @FXML
    private Spinner<Integer> triesBeforeOverrideGeminiThresholdSoftBlockSpinner;
    @FXML
    private Spinner<Integer> triesBeforeOverrideGeminiThresholdHardBlockSpinner;
    @FXML
    private Spinner<Long> waitAfterErrorSpinner;
    @FXML
    private ComboBox<GeminiSafetySetting.SafetyThreshold> geminiHarassmentSettingComboBox;
    @FXML
    private ComboBox<GeminiSafetySetting.SafetyThreshold> geminiHateSpeechSettingComboBox;
    @FXML
    private ComboBox<GeminiSafetySetting.SafetyThreshold> geminiSexuallyExplicitSettingComboBox;
    @FXML
    private ComboBox<GeminiSafetySetting.SafetyThreshold> geminiDangerousContentSettingComboBox;
    @FXML
    private CheckBox overrideGeminiSafetyThresholdSoftBlock;
    @FXML
    private CheckBox overrideGeminiSafetyThresholdHardBlock;
    @FXML
    private Spinner<Integer> triesPerGeminiThresholdOverrideSoftBlockSpinner;
    @FXML
    private Spinner<Integer> triesPerGeminiThresholdOverrideHardBlockSpinner;
    @FXML
    private CheckBox geminiThresholdOverrideSkipLowLevelsCheckBox;
    @FXML
    private Button openConsoleWindowButton;

    @Nullable
    private TranslationProcess translationProcess = null;

    @FXML
    protected void initialize() {

        this.sourceLangTextField.setText(Backend.getOptions().sourceLanguage.getValue());
        this.targetLangTextField.setText(Backend.getOptions().targetLanguage.getValue());
        this.inputDirLabel.setText(Backend.getOptions().inputDirectory.getValue());
        this.outputDirLabel.setText(Backend.getOptions().outputDirectory.getValue());
        this.geminiApiKeyTextField.setText(Backend.getOptions().geminiApiKey.getValue());
        this.promptTextField.setText(Backend.getOptions().aiPrompt.getValue());

        this.setupIntegerConfigOption(this.linesPerPacketSpinner, Backend.getOptions().linesPerPacket, 1, 10000);
        this.setupIntegerConfigOption(this.triesBeforeStopInvalidLineCountSpinner, Backend.getOptions().triesBeforeErrorInvalidLineCount, 1, 10000);
        this.setupIntegerConfigOption(this.triesBeforeStopTimeoutSpinner, Backend.getOptions().triesBeforeErrorTimeoutOrConnectionFailed, 1, 10000);
        this.setupIntegerConfigOption(this.triesBeforeStopGenericErrorSpinner, Backend.getOptions().triesBeforeErrorGeneric, 1, 10000);
        this.setupIntegerConfigOption(this.triesBeforeStopGeminiSoftBlockSpinner, Backend.getOptions().geminiTriesBeforeErrorSoftBlock, 1, 10000);
        this.setupIntegerConfigOption(this.triesBeforeStopGeminiHardBlockSpinner, Backend.getOptions().geminiTriesBeforeErrorHardBlock, 1, 10000);
        this.setupIntegerConfigOption(this.triesBeforeOverrideGeminiThresholdSoftBlockSpinner, Backend.getOptions().geminiOverrideSafetyThresholdSoftBlockAfterTries, 1, 10000);
        this.setupIntegerConfigOption(this.triesBeforeOverrideGeminiThresholdHardBlockSpinner, Backend.getOptions().geminiOverrideSafetyThresholdHardBlockAfterTries, 1, 10000);
        this.setupLongConfigOption(this.waitAfterErrorSpinner, Backend.getOptions().waitMillisBeforeNextTry, 1L, 100000000000000L);

        this.setupGeminiSafetyThresholdConfigOption(this.geminiHarassmentSettingComboBox, Backend.getOptions().geminiHarmCategoryHarassmentSetting);
        this.setupGeminiSafetyThresholdConfigOption(this.geminiHateSpeechSettingComboBox, Backend.getOptions().geminiHarmCategoryHateSpeechSetting);
        this.setupGeminiSafetyThresholdConfigOption(this.geminiSexuallyExplicitSettingComboBox, Backend.getOptions().geminiHarmCategorySexuallyExplicitSetting);
        this.setupGeminiSafetyThresholdConfigOption(this.geminiDangerousContentSettingComboBox, Backend.getOptions().geminiHarmCategoryDangerousContentSetting);

        this.setupBooleanConfigOption(this.overrideGeminiSafetyThresholdSoftBlock, Backend.getOptions().geminiOverrideSafetyThresholdSoftBlock);
        this.setupBooleanConfigOption(this.overrideGeminiSafetyThresholdHardBlock, Backend.getOptions().geminiOverrideSafetyThresholdHardBlock);

        this.setupIntegerConfigOption(this.triesPerGeminiThresholdOverrideSoftBlockSpinner, Backend.getOptions().geminiOverrideSafetyThresholdSoftBlockTriesPerLevel, 1, 10000);
        this.setupIntegerConfigOption(this.triesPerGeminiThresholdOverrideHardBlockSpinner, Backend.getOptions().geminiOverrideSafetyThresholdHardBlockTriesPerLevel, 1, 10000);

        this.setupBooleanConfigOption(this.geminiThresholdOverrideSkipLowLevelsCheckBox, Backend.getOptions().geminiOverrideSafetyThresholdSkipLowLevels);

        this.updateStartTranslationButtonState();

        Timeline tickTimeline = new Timeline(new KeyFrame(Duration.millis(100), actionEvent -> this.tick()));
        tickTimeline.setCycleCount(Animation.INDEFINITE);
        tickTimeline.play();

    }

    protected void tick() {

        //Update UI after translation process finished
        if ((this.translationProcess != null) && !this.translationProcess.running) {
            this.translationProcess = null;
            this.startTranslationButton.setText("Start Translation Process");
            this.toggleAllConfigInputs(false);
        }

        //Update progress bars
        if (this.translationProcess != null) {
            this.subtitleProgressBar.setProgress(this.translationProcess.getCurrentSubtitleProcess());
            this.totalProgressBar.setProgress(this.translationProcess.getTotalProcess());
        } else {
            this.subtitleProgressBar.setProgress(0.0);
            this.totalProgressBar.setProgress(0.0);
        }

    }

    @FXML
    protected void onStartTranslationButtonClick() {
        try {
            if (this.translationProcess == null) {
                this.toggleAllConfigInputs(true);
                this.translationProcess = Backend.translate();
                this.startTranslationButton.setText("Stop Translation Process");
            } else {
                this.translationProcess.running = false;
                this.translationProcess = null;
                this.startTranslationButton.setText("Start Translation Process");
                this.toggleAllConfigInputs(false);
            }
        } catch (Exception ex) {
            LOGGER.error("Error while trying to start translation process!", ex);
            if (this.translationProcess != null) {
                this.translationProcess.running = false;
                this.translationProcess = null;
                this.startTranslationButton.setText("Start Translation Process");
            }
            this.toggleAllConfigInputs(false);
        }
    }

    @FXML
    protected void onOpenConsoleWindowButtonClick() {

       try {

           Stage stageConsoleWindow = new Stage();
           FXMLLoader fxmlLoader = new FXMLLoader(PolyglotApplication.class.getResource("console-view.fxml"));
           Scene scene = new Scene(fxmlLoader.load(), 830, 826);
           stageConsoleWindow.setTitle("Console Output");
           stageConsoleWindow.setScene(scene);
           stageConsoleWindow.show();

       } catch (Exception ex) {
           LOGGER.error("Failed to open console window!", ex);
       }

    }

    @FXML
    protected void onPromptTextFieldInput() {
        String s = this.promptTextField.getText();
        if (s != null) Backend.getOptions().aiPrompt.setValue(s);
        this.updateStartTranslationButtonState();
    }

    @FXML
    protected void onApiKeyTextFieldInput() {
        String s = this.geminiApiKeyTextField.getText();
        if (s != null) Backend.getOptions().geminiApiKey.setValue(s);
        this.updateStartTranslationButtonState();
    }

    @FXML
    protected void onSourceLangTextFieldInput() {
        String s = this.sourceLangTextField.getText();
        if (s != null) Backend.getOptions().sourceLanguage.setValue(s);
        this.updateStartTranslationButtonState();
    }

    @FXML
    protected void onTargetLangTextFieldInput() {
        String s = this.targetLangTextField.getText();
        if (s != null) Backend.getOptions().targetLanguage.setValue(s);
        this.updateStartTranslationButtonState();
    }

    @FXML
    protected void onChooseInputDirButtonClick() {

        if (PolyglotApplication.stage == null) return;

        File inputDirParent = null;
        if (!Backend.getOptions().inputDirectory.getValue().trim().isEmpty()) {
            inputDirParent = new File(Backend.getOptions().inputDirectory.getValue());
            inputDirParent = inputDirParent.getParentFile();
        }

        DirectoryChooser chooser = new DirectoryChooser();
        if (inputDirParent != null) {
            chooser.setInitialDirectory(inputDirParent);
        }
        File selectedDirectory = chooser.showDialog(PolyglotApplication.stage);
        if (selectedDirectory == null) return;

        Backend.getOptions().inputDirectory.setValue(selectedDirectory.getPath().replace("\\", "/"));

        this.inputDirLabel.setText(Backend.getOptions().inputDirectory.getValue());
        this.updateStartTranslationButtonState();

    }

    @FXML
    protected void onChooseOutputDirButtonClick() {

        if (PolyglotApplication.stage == null) return;

        File outputDirParent = null;
        if (!Backend.getOptions().outputDirectory.getValue().trim().isEmpty()) {
            outputDirParent = new File(Backend.getOptions().outputDirectory.getValue());
            outputDirParent = outputDirParent.getParentFile();
        }

        DirectoryChooser chooser = new DirectoryChooser();
        if (outputDirParent != null) {
            chooser.setInitialDirectory(outputDirParent);
        }
        File selectedDirectory = chooser.showDialog(PolyglotApplication.stage);
        if (selectedDirectory == null) return;

        Backend.getOptions().outputDirectory.setValue(selectedDirectory.getPath().replace("\\", "/"));

        this.outputDirLabel.setText(Backend.getOptions().outputDirectory.getValue());
        this.updateStartTranslationButtonState();

    }

    protected void updateStartTranslationButtonState() {
        this.startTranslationButton.setDisable(
                Backend.getOptions().geminiApiKey.getValue().trim().isEmpty()
                        || Backend.getOptions().inputDirectory.getValue().trim().isEmpty()
                        || Backend.getOptions().outputDirectory.getValue().trim().isEmpty()
                        || Backend.getOptions().sourceLanguage.getValue().trim().isEmpty()
                        || Backend.getOptions().targetLanguage.getValue().trim().isEmpty());
    }

    protected void toggleAllConfigInputs(boolean disabled) {
        this.chooseInputDirButton.setDisable(disabled);
        this.chooseOutputDirButton.setDisable(disabled);
        this.targetLangTextField.setDisable(disabled);
        this.sourceLangTextField.setDisable(disabled);
        this.promptTextField.setDisable(disabled);
        this.geminiApiKeyTextField.setDisable(disabled);
        this.linesPerPacketSpinner.setDisable(disabled);
    }

    protected void setupIntegerConfigOption(@NotNull Spinner<Integer> spinner, @NotNull AbstractOptions.Option<Integer> option, int minValue, int maxValue) {
        if (option.getValue() < minValue) option.setValue(minValue);
        if (option.getValue() > maxValue) option.setValue(maxValue);
        SpinnerUtils.prepareIntegerSpinner(spinner, minValue, maxValue, option.getValue(), (oldValue, newValue) -> {
            option.setValue(newValue);
            if (option.getValue() < minValue) option.setValue(minValue);
            if (option.getValue() > maxValue) option.setValue(maxValue);
            this.updateStartTranslationButtonState();
        });
    }

    protected void setupLongConfigOption(@NotNull Spinner<Long> spinner, @NotNull AbstractOptions.Option<Long> option, long minValue, long maxValue) {
        if (option.getValue() < minValue) option.setValue(minValue);
        if (option.getValue() > maxValue) option.setValue(maxValue);
        SpinnerUtils.prepareLongSpinner(spinner, minValue, maxValue, option.getValue(), (oldValue, newValue) -> {
            option.setValue(newValue);
            if (option.getValue() < minValue) option.setValue(minValue);
            if (option.getValue() > maxValue) option.setValue(maxValue);
            this.updateStartTranslationButtonState();
        });
    }

    protected void setupBooleanConfigOption(@NotNull CheckBox checkBox, @NotNull AbstractOptions.Option<Boolean> option) {
        checkBox.selectedProperty().addListener((observable, oldValue, newValue) -> option.setValue(newValue));
        checkBox.setSelected(option.getValue());
    }

    protected void setupGeminiSafetyThresholdConfigOption(@NotNull ComboBox<GeminiSafetySetting.SafetyThreshold> comboBox, @NotNull AbstractOptions.Option<String> option) {
        comboBox.setConverter(new StringConverter<>() {
            @Override
            public String toString(GeminiSafetySetting.SafetyThreshold object) {
                return object.name;
            }
            @Override
            public GeminiSafetySetting.SafetyThreshold fromString(String string) {
                return GeminiSafetySetting.SafetyThreshold.getByName(string);
            }
        });
        comboBox.getItems().addAll(GeminiSafetySetting.SafetyThreshold.values());
        comboBox.valueProperty().addListener((observable, oldValue, newValue) -> option.setValue(newValue.name));
        comboBox.setValue(GeminiSafetySetting.SafetyThreshold.getByName(option.getValue()));
    }

}