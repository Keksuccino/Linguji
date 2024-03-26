package de.keksuccino.linguji.linguji.frontend.views;

import de.keksuccino.linguji.linguji.backend.Backend;
import de.keksuccino.linguji.linguji.backend.lib.ffmpeg.Ffmpeg;
import de.keksuccino.linguji.linguji.backend.lib.ffmpeg.info.VideoInfo;
import de.keksuccino.linguji.linguji.backend.lib.ffmpeg.info.VideoStream;
import de.keksuccino.linguji.linguji.backend.lib.mkvtoolnix.MkvToolNix;
import de.keksuccino.linguji.linguji.backend.subtitle.translation.TranslationProcess;
import de.keksuccino.linguji.linguji.backend.translator.FallbackTranslatorBehaviour;
import de.keksuccino.linguji.linguji.backend.translator.TranslationEngineBuilder;
import de.keksuccino.linguji.linguji.backend.translator.TranslationEngines;
import de.keksuccino.linguji.linguji.backend.translator.gemini.safety.GeminiSafetySetting;
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
    private TextField deeplxApiUrlTextField;
    @FXML
    private TextField libreApiUrlTextField;
    @FXML
    private TextField libreApiKeyTextField;
    @FXML
    private Spinner<Integer> deeplxTriesBeforeStopEmptyResponseSpinner;
    @FXML
    private CheckBox setVideoSubtitleAsDefaultCheckBox;

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
        this.setupStringConfigOption(this.deeplxApiUrlTextField, Backend.getOptions().deepLxUrl);
        this.setupStringConfigOption(this.libreApiUrlTextField, Backend.getOptions().libreTranslateUrl);
        this.setupStringConfigOption(this.libreApiKeyTextField, Backend.getOptions().libreTranslateApiKey);
        this.setupIntegerConfigOption(this.deeplxTriesBeforeStopEmptyResponseSpinner, Backend.getOptions().deepLxTriesBeforeErrorEmptyResponse, 1, 10000);
        this.setupBooleanConfigOption(this.setVideoSubtitleAsDefaultCheckBox, Backend.getOptions().setVideoSubtitleAsDefault);

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
            controller.subtitles = info.getSubtitlesOfType("ass");

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
        if (s != null) Backend.getOptions().geminiApiKey.setValue(s);
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
        this.startTranslationButton.setDisable(
                Backend.getOptions().geminiApiKey.getValue().trim().isEmpty()
                        || Backend.getOptions().inputDirectory.getValue().trim().isEmpty()
                        || Backend.getOptions().outputDirectory.getValue().trim().isEmpty());
    }

    protected void toggleAllConfigInputs(boolean disabled) {
        this.chooseInputDirButton.setDisable(disabled);
        this.chooseOutputDirButton.setDisable(disabled);
        this.sourceLangComboBox.setDisable(disabled);
        this.targetLangComboBox.setDisable(disabled);
        this.promptTextField.setDisable(disabled);
        this.geminiApiKeyTextField.setDisable(disabled);
        this.linesPerPacketSpinner.setDisable(disabled);
        this.primaryTranslationEngineComboBox.setDisable(disabled);
        this.fallbackTranslationEngineComboBox.setDisable(disabled);
        this.waitBetweenRequestsSpinner.setDisable(disabled);
        this.setVideoSubtitleAsDefaultCheckBox.setDisable(disabled);
        this.deeplApiKeyTextField.setDisable(disabled);
        this.deeplxApiUrlTextField.setDisable(disabled);
        this.libreApiKeyTextField.setDisable(disabled);
        this.libreApiUrlTextField.setDisable(disabled);
        this.fallbackTranslatorBehaviourComboBox.setDisable(disabled);
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

}