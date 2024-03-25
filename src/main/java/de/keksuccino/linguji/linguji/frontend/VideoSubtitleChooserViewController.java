package de.keksuccino.linguji.linguji.frontend;

import de.keksuccino.linguji.linguji.backend.lib.MathUtils;
import de.keksuccino.linguji.linguji.backend.lib.ffmpeg.info.VideoStream;
import de.keksuccino.linguji.linguji.backend.lib.logger.LogHandler;
import de.keksuccino.linguji.linguji.backend.lib.logger.SimpleLogger;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.util.List;

public class VideoSubtitleChooserViewController implements ViewControllerBase {

    private static final SimpleLogger LOGGER = LogHandler.getLogger();
    private static final VideoStream EMPTY_STREAM = new VideoStream();

    @FXML
    private ComboBox<VideoStream> subtitleChooserComboBox;

    protected Stage stage;
    protected MainViewController mainViewController;
    protected List<VideoStream> subtitles = null;

    @FXML
    protected void initialize() {

        this.subtitleChooserComboBox.setConverter(new StringConverter<>() {
            @Override
            public String toString(VideoStream object) {
                if (object != null) {
                    if (object == EMPTY_STREAM) {
                        return "No subtitles found!";
                    }
                    if (object.isSubtitle()) {
                        String lang = object.getLanguage();
                        String subtitleType = object.getSubtitleType();
                        String s = "#" + object.index + " Subtitle Track";
                        if (subtitleType != null) s += " (Type: " + subtitleType + ")";
                        if (lang != null) s += " (Language: " + lang + ")";
                        return s;
                    }
                    return "[INVALID SUBTITLE STREAM]";
                }
                return null;
            }
            @Override
            public VideoStream fromString(String string) {
                if ((subtitles == null) || (subtitles.isEmpty())) return EMPTY_STREAM;
                if (!string.startsWith("#")) return EMPTY_STREAM;
                string = string.substring(1);
                if (string.contains(" ")) string = string.split(" ", 2)[0];
                if (!MathUtils.isInteger(string)) return EMPTY_STREAM;
                int index = Integer.parseInt(string);
                for (VideoStream stream : subtitles) {
                    if (stream.index == index) return stream;
                }
                return EMPTY_STREAM;
            }
        });

    }

    protected void finishInitialization(@NotNull Stage stage) {

        this.stage = stage;

        stage.setOnCloseRequest(event -> {
            if (this.mainViewController != null) this.mainViewController.subChooserOpen = false;
        });

        this.subtitleChooserComboBox.getItems().clear();
        if ((this.subtitles != null) && !this.subtitles.isEmpty()) {
            this.subtitleChooserComboBox.getItems().clear();
            this.subtitleChooserComboBox.getItems().addAll(this.subtitles);
            this.subtitleChooserComboBox.setValue(this.subtitles.get(0));
        } else {
            this.subtitleChooserComboBox.getItems().add(EMPTY_STREAM);
        }

    }

    @FXML
    protected void onConfirmButtonClick() {
        if (this.mainViewController != null) {
            this.mainViewController.cachedVideoFileSubtitleStream = this.getSelectedSubtitle();
            this.mainViewController.subChooserOpen = false;
            this.mainViewController.toggleTranslationProcess();
        }
        if (this.stage != null) this.stage.close();
    }

    @FXML
    protected void onCancelButtonClick() {
        if (this.mainViewController != null) {
            this.mainViewController.subChooserOpen = false;
        }
        if (this.stage != null) this.stage.close();
    }

    @Nullable
    protected VideoStream getSelectedSubtitle() {
        if (this.subtitleChooserComboBox.getItems().isEmpty()) return null;
        VideoStream stream = this.subtitleChooserComboBox.getValue();
        if (stream == EMPTY_STREAM) return null;
        return stream;
    }

}