package de.keksuccino.linguji.linguji.frontend.views;

import de.keksuccino.linguji.linguji.backend.Backend;
import de.keksuccino.linguji.linguji.backend.lib.ffmpeg.Ffmpeg;
import de.keksuccino.linguji.linguji.backend.lib.ffmpeg.info.VideoInfo;
import de.keksuccino.linguji.linguji.backend.lib.ffmpeg.info.VideoStream;
import de.keksuccino.linguji.linguji.backend.lib.mkvtoolnix.MkvToolNix;
import de.keksuccino.linguji.linguji.backend.subtitle.translation.TranslationProcess;
import de.keksuccino.linguji.linguji.backend.engine.FallbackTranslatorBehaviour;
import de.keksuccino.linguji.linguji.backend.engine.TranslationEngineBuilder;
import de.keksuccino.linguji.linguji.backend.engine.engines.TranslationEngines;
import de.keksuccino.linguji.linguji.backend.engine.engines.gemini.GeminiModel;
import de.keksuccino.linguji.linguji.backend.engine.engines.gemini.GeminiModelFetcher;
import de.keksuccino.linguji.linguji.backend.engine.engines.gemini.safety.GeminiSafetySetting;
import de.keksuccino.linguji.linguji.backend.engine.engines.openrouter.OpenRouterModel;
import de.keksuccino.linguji.linguji.backend.engine.engines.openrouter.OpenRouterTranslationEngine;
import de.keksuccino.linguji.linguji.backend.lib.lang.Locale;
import de.keksuccino.linguji.linguji.backend.lib.logger.LogHandler;
import de.keksuccino.linguji.linguji.backend.lib.logger.SimpleLogger;
import de.keksuccino.linguji.linguji.backend.lib.options.AbstractOptions;
import de.keksuccino.linguji.linguji.backend.lib.os.OSUtils;
import de.keksuccino.linguji.linguji.frontend.Frontend;
import de.keksuccino.linguji.linguji.frontend.TaskExecutor;
import de.keksuccino.linguji.linguji.frontend.util.SpinnerUtils;
import de.keksuccino.linguji.linguji.frontend.util.os.windows.FXWinUtil;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.stage.DirectoryChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.util.StringConverter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MainViewController implements ViewControllerBase {

    private static final SimpleLogger LOGGER = LogHandler.getLogger();

    @FXML
    private ScrollPane contentScrollPane;
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
    private ComboBox<Locale> sourceLangComboBox;
    @FXML
    private ComboBox<Locale> targetLangComboBox;
    @FXML
    private TextField geminiApiKeyTextField;
    @FXML
    private ComboBox<GeminiModel> geminiModelComboBox;
    @FXML
    private Button refreshGeminiModelsButton;
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
    private Spinner<Long> waitBetweenRequestsSpinner;
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
    private ComboBox<FallbackTranslatorBehaviour> fallbackTranslatorBehaviourComboBox;
    @FXML
    private ComboBox<TranslationEngineBuilder<?>> primaryTranslationEngineComboBox;
    @FXML
    private ComboBox<TranslationEngineBuilder<?>> fallbackTranslationEngineComboBox;
    @FXML
    private TextField deeplApiKeyTextField;
    @FXML
    private CheckBox deeplUseProCheckBox;
    @FXML
    private TextField deeplxApiUrlTextField;
    @FXML
    private TextField libreApiUrlTextField;
    @FXML
    private TextField libreApiKeyTextField;
    @FXML
    private Spinner<Integer> deeplxTriesBeforeStopEmptyResponseSpinner;
    @FXML
    private CheckBox setVideoSubtitleAsDefaultCheckBox;
    @FXML
    private TextField openRouterApiKeyTextField;
    @FXML
    private ComboBox<OpenRouterModel> openRouterModelComboBox;
    @FXML
    private Button refreshOpenRouterModelsButton;
    @FXML
    private Spinner<Double> openRouterTemperatureSpinner;
    @FXML
    private Spinner<Integer> openRouterMaxTokensSpinner;

    protected Stage stage;
    @Nullable
    private TranslationProcess translationProcess = null;
    @Nullable
    protected VideoStream cachedVideoFileSubtitleStream = null;
    protected boolean subChooserOpen = false;

    @FXML
    public void initialize() {

        this.inputDirLabel.setText(Backend.getOptions().inputDirectory.getValue());
        this.outputDirLabel.setText(Backend.getOptions().outputDirectory.getValue());
        this.geminiApiKeyTextField.setText(Backend.getOptions().geminiApiKey.getValue());
        this.promptTextField.setText(Backend.getOptions().aiPrompt.getValue());

        this.setupGeminiModelConfigOption();
        this.setupLocaleConfigOption(this.sourceLangComboBox, Backend.getOptions().sourceLanguageLocale);
        this.setupLocaleConfigOption(this.targetLangComboBox, Backend.getOptions().targetLanguageLocale);
        this.setupIntegerConfigOption(this.linesPerPacketSpinner, Backend.getOptions().linesPerPacket, 1, 10000);
        this.setupIntegerConfigOption(this.triesBeforeStopInvalidLineCountSpinner, Backend.getOptions().triesBeforeErrorInvalidLineCount, 1, 10000);
        this.setupIntegerConfigOption(this.triesBeforeStopTimeoutSpinner, Backend.getOptions().triesBeforeErrorTimeoutOrConnectionFailed, 1, 10000);
        this.setupIntegerConfigOption(this.triesBeforeStopGenericErrorSpinner, Backend.getOptions().triesBeforeErrorGeneric, 1, 10000);
        this.setupIntegerConfigOption(this.triesBeforeStopGeminiSoftBlockSpinner, Backend.getOptions().geminiTriesBeforeErrorSoftBlock, 1, 10000);
        this.setupIntegerConfigOption(this.triesBeforeStopGeminiHardBlockSpinner, Backend.getOptions().geminiTriesBeforeErrorHardBlock, 1, 10000);
        this.setupIntegerConfigOption(this.triesBeforeOverrideGeminiThresholdSoftBlockSpinner, Backend.getOptions().geminiOverrideSafetyThresholdSoftBlockAfterTries, 1, 10000);
        this.setupIntegerConfigOption(this.triesBeforeOverrideGeminiThresholdHardBlockSpinner, Backend.getOptions().geminiOverrideSafetyThresholdHardBlockAfterTries, 1, 10000);
        this.setupLongConfigOption(this.waitBetweenRequestsSpinner, Backend.getOptions().waitMillisBetweenRequests, 1L, 100000000000000L);
        this.setupGeminiSafetyThresholdConfigOption(this.geminiHarassmentSettingComboBox, Backend.getOptions().geminiHarmCategoryHarassmentSetting);
        this.setupGeminiSafetyThresholdConfigOption(this.geminiHateSpeechSettingComboBox, Backend.getOptions().geminiHarmCategoryHateSpeechSetting);
        this.setupGeminiSafetyThresholdConfigOption(this.geminiSexuallyExplicitSettingComboBox, Backend.getOptions().geminiHarmCategorySexuallyExplicitSetting);
        this.setupGeminiSafetyThresholdConfigOption(this.geminiDangerousContentSettingComboBox, Backend.getOptions().geminiHarmCategoryDangerousContentSetting);
        this.setupBooleanConfigOption(this.overrideGeminiSafetyThresholdSoftBlock, Backend.getOptions().geminiOverrideSafetyThresholdSoftBlock);
        this.setupBooleanConfigOption(this.overrideGeminiSafetyThresholdHardBlock, Backend.getOptions().geminiOverrideSafetyThresholdHardBlock);
        this.setupIntegerConfigOption(this.triesPerGeminiThresholdOverrideSoftBlockSpinner, Backend.getOptions().geminiOverrideSafetyThresholdSoftBlockTriesPerLevel, 1, 10000);
        this.setupIntegerConfigOption(this.triesPerGeminiThresholdOverrideHardBlockSpinner, Backend.getOptions().geminiOverrideSafetyThresholdHardBlockTriesPerLevel, 1, 10000);
        this.setupBooleanConfigOption(this.geminiThresholdOverrideSkipLowLevelsCheckBox, Backend.getOptions().geminiOverrideSafetyThresholdSkipLowLevels);
        this.setupTranslationEngineBuilderConfigOption(this.primaryTranslationEngineComboBox, Backend.getOptions().primaryTranslationEngine);
        this.setupTranslationEngineBuilderConfigOption(this.fallbackTranslationEngineComboBox, Backend.getOptions().fallbackTranslationEngine);
        this.setupFallbackTranslationBehaviourConfigOption(this.fallbackTranslatorBehaviourComboBox, Backend.getOptions().fallbackTranslatorBehaviour);
        this.setupStringConfigOption(this.deeplApiKeyTextField, Backend.getOptions().deepLApiKey);
        this.setupBooleanConfigOption(this.deeplUseProCheckBox, Backend.getOptions().deepLUsePro);
        this.setupStringConfigOption(this.deeplxApiUrlTextField, Backend.getOptions().deepLxUrl);
        this.setupStringConfigOption(this.libreApiUrlTextField, Backend.getOptions().libreTranslateUrl);
        this.setupStringConfigOption(this.libreApiKeyTextField, Backend.getOptions().libreTranslateApiKey);
        this.setupIntegerConfigOption(this.deeplxTriesBeforeStopEmptyResponseSpinner, Backend.getOptions().deepLxTriesBeforeErrorEmptyResponse, 1, 10000);
        this.setupBooleanConfigOption(this.setVideoSubtitleAsDefaultCheckBox, Backend.getOptions().setVideoSubtitleAsDefault);
        this.setupStringConfigOption(this.openRouterApiKeyTextField, Backend.getOptions().openRouterApiKey);
        this.setupOpenRouterModelConfigOption();
        this.setupDoubleConfigOption(this.openRouterTemperatureSpinner, Backend.getOptions().openRouterTemperature, 0.0, 2.0);
        this.setupIntegerConfigOption(this.openRouterMaxTokensSpinner, Backend.getOptions().openRouterMaxTokens, 1, 100000);

        //Fix slow scrolling in content ScrollPane
        this.contentScrollPane.getContent().setOnScroll(scrollEvent -> {
            double deltaY = scrollEvent.getDeltaY();
            double contentHeight = this.contentScrollPane.getContent().getBoundsInLocal().getHeight();
            double scrollPaneHeight = this.contentScrollPane.getHeight();
            double diff = contentHeight - scrollPaneHeight;
            if (diff < 1) diff = 1;
            double vValue = this.contentScrollPane.getVvalue();
            this.contentScrollPane.setVvalue(vValue + -deltaY/diff);
        });

        this.updateStartTranslationButtonState();

        Timeline tickTimeline = new Timeline(new KeyFrame(Duration.millis(100), actionEvent -> this.tick()));
        tickTimeline.setCycleCount(Animation.INDEFINITE);
        tickTimeline.play();

    }

    public void finishInitialization(@NotNull Stage stage, @NotNull Parent parent) {

        this.stage = stage;

        //Modify all tooltips
        getAllNodes(parent).forEach(node -> {
            if (node instanceof Control control) {
                if (control.getTooltip() != null) {
                    control.getTooltip().setShowDelay(Duration.millis(100));
                }
            }
        });

        //Clear temp dir on close
        stage.setOnCloseRequest(event -> Backend.resetTempDirectory());

    }

    protected void tick() {

        TaskExecutor.runQueuedTasks();

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
    protected void onOpenConsoleWindowButtonClick() {

        if (this.subChooserOpen) return;

        try {

            Stage stageConsoleWindow = new Stage();
            stageConsoleWindow.initOwner(this.stage);
            stageConsoleWindow.setAlwaysOnTop(false);
            FXMLLoader fxmlLoader = new FXMLLoader(ConsoleViewController.class.getResource("console-view.fxml"));
            Parent root = fxmlLoader.load();
            ConsoleViewController controller = fxmlLoader.getController();
            controller.finishInitialization(stageConsoleWindow);
            Scene scene = new Scene(root, 1067, 634);
            stageConsoleWindow.setTitle("Console Output");
            stageConsoleWindow.setScene(scene);
            stageConsoleWindow.show();

            if (OSUtils.isWindows()) {
                try {
                    FXWinUtil.setDarkMode(stageConsoleWindow, true);
                } catch (Exception ex) {
                    LOGGER.error("Failed to set theme of Windows title bar!", ex);
                }
            }

        } catch (Exception ex) {
            LOGGER.error("Failed to open console window!", ex);
        }

    }

    @FXML
    protected void onStartTranslationButtonClick() {
        if (this.subChooserOpen) return;
        if (this.translationProcess == null) {
            //Video support Windows-only for now
            if (OSUtils.isWindows()) {
                File videoFile = Backend.getFirstVideoOfInputDirectory();
                if (videoFile != null) {
                    if (!Ffmpeg.readyToBuildDefaultInstance()) {
                        this.openFfmpegMissingPopup();
                    } else if (!MkvToolNix.readyToBuildDefaultInstance()) {
                        this.openMkvToolNixMissingPopup();
                    } else {
                        this.openSubtitleChooser(videoFile);
                    }
                } else {
                    this.toggleTranslationProcess();
                }
            } else {
                this.toggleTranslationProcess();
            }
        } else {
            this.toggleTranslationProcess();
        }
    }

    protected void toggleTranslationProcess() {
        try {
            if (this.translationProcess == null) {
                VideoStream v = this.cachedVideoFileSubtitleStream;
                this.cachedVideoFileSubtitleStream = null;
                this.toggleAllConfigInputs(true);
                this.translationProcess = Backend.translate(v);
                this.startTranslationButton.setText("Stop Translation Process");
            } else {
                this.translationProcess.stoppedByUser = true;
                this.translationProcess.running = false;
                this.translationProcess = null;
                this.startTranslationButton.setText("Start Translation Process");
                this.toggleAllConfigInputs(false);
            }
        } catch (Exception ex) {
            LOGGER.error("Error while trying to start translation process!", ex);
            if (this.translationProcess != null) {
                this.translationProcess.stoppedByUser = true;
                this.translationProcess.running = false;
                this.translationProcess = null;
                this.startTranslationButton.setText("Start Translation Process");
            }
            this.toggleAllConfigInputs(false);
        }
    }

    protected void openSubtitleChooser(@NotNull File videoFile) {

        try {

            Stage stageChooserWindow = new Stage();
            stageChooserWindow.initOwner(this.stage);
            //blocks the main view if the chooser is open
            stageChooserWindow.initModality(Modality.WINDOW_MODAL);
            FXMLLoader loader = new FXMLLoader(VideoSubtitleChooserViewController.class.getResource("video-subtitle-chooser-view.fxml"));
            Parent root = loader.load();

            VideoSubtitleChooserViewController controller = loader.getController();

            controller.mainViewController = this;

            Ffmpeg ffmpeg = Ffmpeg.buildDefault();
            VideoInfo info = Objects.requireNonNull(ffmpeg.getVideoInfo(videoFile));

            //Add subtitle tracks of all supported types
            controller.subtitles = new ArrayList<>();
            controller.subtitles.addAll(info.getSubtitlesOfType("ass"));
            controller.subtitles.addAll(info.getSubtitlesOfType("srt"));
            controller.subtitles.addAll(info.getSubtitlesOfType("subrip")); //that's SRT (not sure if the type is always subrip, so I'm adding both srt and subrip)

            controller.finishInitialization(stageChooserWindow);

            Scene scene = new Scene(root, 568, 272);
            stageChooserWindow.setTitle("Choose Subtitle");
            stageChooserWindow.setScene(scene);
            stageChooserWindow.show();
            Toolkit.getDefaultToolkit().beep();

            this.subChooserOpen = true;

            if (OSUtils.isWindows()) {
                try {
                    FXWinUtil.setDarkMode(stageChooserWindow, true);
                } catch (Exception ex) {
                    LOGGER.error("Failed to set theme of Windows title bar!", ex);
                }
            }

        } catch (Exception ex) {
            LOGGER.error("Failed to open subtitle chooser window!", ex);
        }

    }

    protected void openFfmpegMissingPopup() {

        Frontend.openAlert(
                "FFMPEG not found!",
                "FFMPEG executables not found!",
                "Video files were found in the input directory, but not all\nFFMPEG executables were found in the 'ffmpeg' directory.\n\nMake sure both 'ffmpeg' and 'ffprobe' executables are in the 'ffmpeg' directory,\nwhich is located at the same path as Linguji's executable.");

    }

    protected void openMkvToolNixMissingPopup() {

        Frontend.openAlert(
                "MkvToolNix not found!",
                "MkvToolNix executables not found!",
                "Video files were found in the input directory, but not all\nMkvToolNix executables were found in the 'mkvtoolnix' directory.\n\nMake sure both 'mkvmerge' and 'mkvextract' executables are in the 'mkvtoolnix' directory,\nwhich is located at the same path as Linguji's executable.");

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
        if (s != null) {
            Backend.getOptions().geminiApiKey.setValue(s);
            // Reload models when API key changes
            this.loadGeminiModels();
        }
        this.updateStartTranslationButtonState();
    }

    @FXML
    protected void onChooseInputDirButtonClick() {

        File inputDirParent = null;
        if (!Backend.getOptions().inputDirectory.getValue().trim().isEmpty()) {
            inputDirParent = new File(Backend.getOptions().inputDirectory.getValue());
            inputDirParent = inputDirParent.getParentFile();
        }

        DirectoryChooser chooser = new DirectoryChooser();
        if (inputDirParent != null) {
            chooser.setInitialDirectory(inputDirParent);
        }
        File selectedDirectory = chooser.showDialog(this.stage);
        if (selectedDirectory == null) return;

        Backend.getOptions().inputDirectory.setValue(selectedDirectory.getPath().replace("\\", "/"));

        this.inputDirLabel.setText(Backend.getOptions().inputDirectory.getValue());
        this.updateStartTranslationButtonState();

    }

    @FXML
    protected void onChooseOutputDirButtonClick() {

        File outputDirParent = null;
        if (!Backend.getOptions().outputDirectory.getValue().trim().isEmpty()) {
            outputDirParent = new File(Backend.getOptions().outputDirectory.getValue());
            outputDirParent = outputDirParent.getParentFile();
        }

        DirectoryChooser chooser = new DirectoryChooser();
        if (outputDirParent != null) {
            chooser.setInitialDirectory(outputDirParent);
        }
        File selectedDirectory = chooser.showDialog(this.stage);
        if (selectedDirectory == null) return;

        Backend.getOptions().outputDirectory.setValue(selectedDirectory.getPath().replace("\\", "/"));

        this.outputDirLabel.setText(Backend.getOptions().outputDirectory.getValue());
        this.updateStartTranslationButtonState();

    }

    protected void updateStartTranslationButtonState() {
//        this.startTranslationButton.setDisable(
//                Backend.getOptions().geminiApiKey.getValue().trim().isEmpty()
//                        || Backend.getOptions().inputDirectory.getValue().trim().isEmpty()
//                        || Backend.getOptions().outputDirectory.getValue().trim().isEmpty());
        this.startTranslationButton.setDisable(false);
    }

    protected void toggleAllConfigInputs(boolean disabled) {
        this.chooseInputDirButton.setDisable(disabled);
        this.chooseOutputDirButton.setDisable(disabled);
        this.sourceLangComboBox.setDisable(disabled);
        this.targetLangComboBox.setDisable(disabled);
        this.promptTextField.setDisable(disabled);
        this.geminiApiKeyTextField.setDisable(disabled);
        this.geminiModelComboBox.setDisable(disabled);
        this.refreshGeminiModelsButton.setDisable(disabled);
        this.linesPerPacketSpinner.setDisable(disabled);
        this.primaryTranslationEngineComboBox.setDisable(disabled);
        this.fallbackTranslationEngineComboBox.setDisable(disabled);
        this.waitBetweenRequestsSpinner.setDisable(disabled);
        this.setVideoSubtitleAsDefaultCheckBox.setDisable(disabled);
        this.deeplApiKeyTextField.setDisable(disabled);
        this.deeplUseProCheckBox.setDisable(disabled);
        this.deeplxApiUrlTextField.setDisable(disabled);
        this.libreApiKeyTextField.setDisable(disabled);
        this.libreApiUrlTextField.setDisable(disabled);
        this.fallbackTranslatorBehaviourComboBox.setDisable(disabled);
        this.openRouterApiKeyTextField.setDisable(disabled);
        this.openRouterModelComboBox.setDisable(disabled);
        this.refreshOpenRouterModelsButton.setDisable(disabled);
        this.openRouterTemperatureSpinner.setDisable(disabled);
        this.openRouterMaxTokensSpinner.setDisable(disabled);
    }

    protected void setupStringConfigOption(@NotNull TextField textField, @NotNull AbstractOptions.Option<String> option) {
        textField.setText(option.getValue());
        textField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) option.setValue(newValue);
            this.updateStartTranslationButtonState();
        });
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

    protected void setupDoubleConfigOption(@NotNull Spinner<Double> spinner, @NotNull AbstractOptions.Option<Double> option, double minValue, double maxValue) {
        if (option.getValue() < minValue) option.setValue(minValue);
        if (option.getValue() > maxValue) option.setValue(maxValue);
        SpinnerUtils.prepareDoubleSpinner(spinner, minValue, maxValue, option.getValue(), (oldValue, newValue) -> {
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

    protected void setupLocaleConfigOption(@NotNull ComboBox<Locale> comboBox, @NotNull AbstractOptions.Option<String> option) {
        comboBox.setConverter(new StringConverter<>() {
            @Override
            public String toString(Locale object) {
                return object.getDisplayName();
            }
            @Override
            public Locale fromString(String string) {
                return Locale.getByDisplayName(string);
            }
        });
        comboBox.getItems().addAll(Locale.getOrderedAlphabeticallyByDisplayName());
        comboBox.valueProperty().addListener((observable, oldValue, newValue) -> option.setValue(newValue.getName()));
        comboBox.setValue(Locale.getByName(option.getValue()));
    }

    protected void setupTranslationEngineBuilderConfigOption(@NotNull ComboBox<TranslationEngineBuilder<?>> comboBox, @NotNull AbstractOptions.Option<String> option) {
        comboBox.setConverter(new StringConverter<>() {
            @Override
            public String toString(TranslationEngineBuilder<?> object) {
                return object.getDisplayName();
            }
            @Override
            public TranslationEngineBuilder<?> fromString(String string) {
                return TranslationEngines.getByDisplayName(string);
            }
        });
        comboBox.getItems().addAll(TranslationEngines.getBuilders());
        comboBox.valueProperty().addListener((observable, oldValue, newValue) -> option.setValue(newValue.getName()));
        comboBox.setValue(TranslationEngines.getByName(option.getValue()));
    }

    protected void setupFallbackTranslationBehaviourConfigOption(@NotNull ComboBox<FallbackTranslatorBehaviour> comboBox, @NotNull AbstractOptions.Option<String> option) {
        comboBox.setConverter(new StringConverter<>() {
            @Override
            public String toString(FallbackTranslatorBehaviour object) {
                return object.getDisplayName();
            }
            @Override
            public FallbackTranslatorBehaviour fromString(String string) {
                return FallbackTranslatorBehaviour.getByDisplayName(string);
            }
        });
        comboBox.getItems().addAll(FallbackTranslatorBehaviour.values());
        comboBox.valueProperty().addListener((observable, oldValue, newValue) -> option.setValue(newValue.getName()));
        comboBox.setValue(FallbackTranslatorBehaviour.getByName(option.getValue()));
    }

    protected void setupGeminiModelConfigOption() {
        // Set up the converter for the ComboBox
        this.geminiModelComboBox.setConverter(new StringConverter<>() {
            @Override
            public String toString(GeminiModel model) {
                return model != null ? model.getDisplayName() : "";
            }
            @Override
            public GeminiModel fromString(String string) {
                for (GeminiModel model : geminiModelComboBox.getItems()) {
                    if (model.getDisplayName().equals(string)) {
                        return model;
                    }
                }
                return null;
            }
        });

        // Load models
        this.loadGeminiModels();

        // Set up the value change listener
        this.geminiModelComboBox.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                Backend.getOptions().geminiModel.setValue(newValue.getModelId());
            }
        });
    }

    protected void loadGeminiModels() {
        List<GeminiModel> models = null;
        
        // Try to fetch models from API if API key is available
        String apiKey = Backend.getOptions().geminiApiKey.getValue();
        if (apiKey != null && !apiKey.trim().isEmpty()) {
            models = GeminiModelFetcher.fetchAvailableModels(apiKey);
        }
        
        // If fetching failed or no API key, use fallback models
        if (models == null || models.isEmpty()) {
            models = GeminiModelFetcher.getFallbackModels();
        }
        
        // Clear and add models to ComboBox
        this.geminiModelComboBox.getItems().clear();
        this.geminiModelComboBox.getItems().addAll(models);
        
        // Select the saved model or default
        String savedModelId = Backend.getOptions().geminiModel.getValue();
        GeminiModel selectedModel = null;
        
        for (GeminiModel model : models) {
            if (model.getModelId().equals(savedModelId)) {
                selectedModel = model;
                break;
            }
        }
        
        // If saved model not found, select the first one
        if (selectedModel == null && !models.isEmpty()) {
            selectedModel = models.get(0);
        }
        
        this.geminiModelComboBox.setValue(selectedModel);
    }

    @FXML
    protected void onRefreshGeminiModelsButtonClick() {
        String apiKey = Backend.getOptions().geminiApiKey.getValue();
        if (apiKey == null || apiKey.trim().isEmpty()) {
            Frontend.openAlert(
                    "No API Key",
                    "Gemini API key not configured",
                    "Please enter a valid Gemini API key before refreshing the model list.");
            return;
        }
        
        // Show that we're refreshing
        this.refreshGeminiModelsButton.setDisable(true);
        this.refreshGeminiModelsButton.setText("Loading...");
        
        // Run in a separate thread to avoid blocking UI
        Thread refreshThread = new Thread(() -> {
            List<GeminiModel> models = GeminiModelFetcher.fetchAvailableModels(apiKey);
            
            // Update UI on JavaFX thread
            TaskExecutor.queueTask(() -> {
                if (models != null && !models.isEmpty()) {
                    // Save current selection
                    GeminiModel currentSelection = this.geminiModelComboBox.getValue();
                    String currentModelId = currentSelection != null ? currentSelection.getModelId() : null;
                    
                    // Update models
                    this.geminiModelComboBox.getItems().clear();
                    this.geminiModelComboBox.getItems().addAll(models);
                    
                    // Try to restore selection
                    if (currentModelId != null) {
                        for (GeminiModel model : models) {
                            if (model.getModelId().equals(currentModelId)) {
                                this.geminiModelComboBox.setValue(model);
                                break;
                            }
                        }
                    }
                    
                    // If previous selection not found, select first
                    if (this.geminiModelComboBox.getValue() == null && !models.isEmpty()) {
                        this.geminiModelComboBox.setValue(models.get(0));
                    }
                    
                    Frontend.openAlert(
                            "Models Refreshed",
                            "Successfully refreshed Gemini models",
                            "Found " + models.size() + " available Gemini models.");
                } else {
                    Frontend.openAlert(
                            "Refresh Failed",
                            "Failed to refresh Gemini models",
                            "Could not fetch models from the Gemini API. Using fallback models.");
                    this.loadGeminiModels(); // Load fallback models
                }
                
                // Re-enable button
                this.refreshGeminiModelsButton.setDisable(false);
                this.refreshGeminiModelsButton.setText("Refresh");
            });
        });
        
        refreshThread.setDaemon(true);
        refreshThread.start();
    }

    protected void setupOpenRouterModelConfigOption() {
        // Set up the converter for the ComboBox
        this.openRouterModelComboBox.setConverter(new StringConverter<>() {
            @Override
            public String toString(OpenRouterModel model) {
                return model != null ? model.getDisplayName() : "";
            }
            @Override
            public OpenRouterModel fromString(String string) {
                for (OpenRouterModel model : openRouterModelComboBox.getItems()) {
                    if (model.getDisplayName().equals(string)) {
                        return model;
                    }
                }
                return null;
            }
        });

        // Load models
        this.loadOpenRouterModels();

        // Set up the value change listener
        this.openRouterModelComboBox.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                Backend.getOptions().openRouterModel.setValue(newValue.getId());
            }
        });
    }

    protected void loadOpenRouterModels() {
        // Always create engine instance - it will handle missing API key gracefully
        OpenRouterTranslationEngine engine = new OpenRouterTranslationEngine();
        
        List<OpenRouterModel> models = engine.getAvailableModels();
        
        // Clear and add models to ComboBox
        this.openRouterModelComboBox.getItems().clear();
        this.openRouterModelComboBox.getItems().addAll(models);
        
        // Select the saved model or default
        String savedModelId = Backend.getOptions().openRouterModel.getValue();
        OpenRouterModel selectedModel = null;
        
        for (OpenRouterModel model : models) {
            if (model.getId().equals(savedModelId)) {
                selectedModel = model;
                break;
            }
        }
        
        // If saved model not found, select the first one
        if (selectedModel == null && !models.isEmpty()) {
            selectedModel = models.get(0);
        }
        
        this.openRouterModelComboBox.setValue(selectedModel);
    }

    @FXML
    protected void onOpenRouterApiKeyTextFieldInput() {
        String s = this.openRouterApiKeyTextField.getText();
        if (s != null) {
            Backend.getOptions().openRouterApiKey.setValue(s);
            // Reload models when API key changes
            this.loadOpenRouterModels();
        }
        this.updateStartTranslationButtonState();
    }

    @FXML
    protected void onRefreshOpenRouterModelsButtonClick() {
        String apiKey = Backend.getOptions().openRouterApiKey.getValue();
        if (apiKey == null || apiKey.trim().isEmpty()) {
            Frontend.openAlert(
                    "No API Key",
                    "OpenRouter API key not configured",
                    "Please enter a valid OpenRouter API key before refreshing the model list.");
            return;
        }
        
        // Show that we're refreshing
        this.refreshOpenRouterModelsButton.setDisable(true);
        this.refreshOpenRouterModelsButton.setText("Loading...");
        
        // Run in a separate thread to avoid blocking UI
        Thread refreshThread = new Thread(() -> {
            // Create engine instance and refresh models
            OpenRouterTranslationEngine engine = new OpenRouterTranslationEngine();
            engine.refreshAvailableModels();
            List<OpenRouterModel> models = engine.getAvailableModels();
            
            // Update UI on JavaFX thread
            TaskExecutor.queueTask(() -> {
                if (models != null && !models.isEmpty()) {
                    // Save current selection
                    OpenRouterModel currentSelection = this.openRouterModelComboBox.getValue();
                    String currentModelId = currentSelection != null ? currentSelection.getId() : null;
                    
                    // Update models
                    this.openRouterModelComboBox.getItems().clear();
                    this.openRouterModelComboBox.getItems().addAll(models);
                    
                    // Try to restore selection
                    if (currentModelId != null) {
                        for (OpenRouterModel model : models) {
                            if (model.getId().equals(currentModelId)) {
                                this.openRouterModelComboBox.setValue(model);
                                break;
                            }
                        }
                    }
                    
                    // If previous selection not found, select first
                    if (this.openRouterModelComboBox.getValue() == null && !models.isEmpty()) {
                        this.openRouterModelComboBox.setValue(models.get(0));
                    }
                    
                    Frontend.openAlert(
                            "Models Refreshed",
                            "Successfully refreshed OpenRouter models",
                            "Found " + models.size() + " available OpenRouter models.");
                } else {
                    Frontend.openAlert(
                            "Refresh Failed",
                            "Failed to refresh OpenRouter models",
                            "Could not fetch models from the OpenRouter API. Using default models.");
                    this.loadOpenRouterModels(); // Load default models
                }
                
                // Re-enable button
                this.refreshOpenRouterModelsButton.setDisable(false);
                this.refreshOpenRouterModelsButton.setText("Refresh");
            });
        });
        
        refreshThread.setDaemon(true);
        refreshThread.start();
    }

}