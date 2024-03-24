package de.keksuccino.linguji.linguji.frontend;

import de.keksuccino.linguji.linguji.backend.lib.logger.LogHandler;
import de.keksuccino.linguji.linguji.backend.lib.logger.SimpleLogger;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.stage.Stage;
import org.jetbrains.annotations.NotNull;

public class VideoSubtitleChooserViewController implements ViewControllerBase {

    private static final SimpleLogger LOGGER = LogHandler.getLogger();

    @FXML
    private ComboBox<String> subtitleChooserComboBox;
    @FXML
    private Button confirmButton;

    @FXML
    protected void initialize() {

    }

    protected void finishInitialization(@NotNull Stage stage) {

    }

    @FXML
    protected void onConfirmButtonClick() {

    }

}