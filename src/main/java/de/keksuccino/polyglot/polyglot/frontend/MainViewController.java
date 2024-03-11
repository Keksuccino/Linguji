package de.keksuccino.polyglot.polyglot.frontend;

import de.keksuccino.polyglot.polyglot.backend.Backend;
import de.keksuccino.polyglot.polyglot.backend.subtitle.translation.TranslationProcess;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.DirectoryChooser;
import javafx.util.Duration;
import org.jetbrains.annotations.Nullable;
import java.io.File;

public class MainViewController {

    @FXML
    private ProgressBar subtitleProgressBar;
    @FXML
    private ProgressBar totalProgressBar;
    @FXML
    private ListView<?> subtitleLinesListView;
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

    @Nullable
    private TranslationProcess translationProcess = null;

    @FXML
    protected void initialize() {

        this.sourceLangTextField.setText(Backend.getOptions().sourceLanguage.getValue());
        this.targetLangTextField.setText(Backend.getOptions().targetLanguage.getValue());

        this.inputDirLabel.setText("Input Directory: " + Backend.getOptions().inputDirectory.getValue());
        this.outputDirLabel.setText("Output Directory: " + Backend.getOptions().outputDirectory.getValue());

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
            ex.printStackTrace();
            if (this.translationProcess != null) {
                this.translationProcess.running = false;
                this.translationProcess = null;
                this.startTranslationButton.setText("Start Translation Process");
            }
            this.toggleAllConfigInputs(false);
        }
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

        this.inputDirLabel.setText("Input Directory: " + Backend.getOptions().inputDirectory.getValue());
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

        this.outputDirLabel.setText("Output Directory: " + Backend.getOptions().outputDirectory.getValue());
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
    }

}