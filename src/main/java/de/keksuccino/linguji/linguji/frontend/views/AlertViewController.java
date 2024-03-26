package de.keksuccino.linguji.linguji.frontend.views;

import de.keksuccino.linguji.linguji.backend.lib.logger.LogHandler;
import de.keksuccino.linguji.linguji.backend.lib.logger.SimpleLogger;
import de.keksuccino.linguji.linguji.backend.lib.os.OSUtils;
import de.keksuccino.linguji.linguji.frontend.util.os.windows.FXWinUtil;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.awt.*;

public class AlertViewController implements ViewControllerBase {

    private static final SimpleLogger LOGGER = LogHandler.getLogger();

    @FXML
    protected Label headerLabel;
    @FXML
    protected Label textLabel;

    private Stage stage;
    protected String header;
    protected String text;

    public static void openAlert(@NotNull String title, @NotNull String header, @NotNull String text, int width, int height, @Nullable Stage parent) {

        try {

            Stage stage = new Stage();
            if (parent != null) {
                stage.initOwner(parent);
                stage.initModality(Modality.WINDOW_MODAL);
                stage.setAlwaysOnTop(true);
            }
            FXMLLoader loader = new FXMLLoader(AlertViewController.class.getResource("alert-view.fxml"));
            Parent root = loader.load();
            AlertViewController controller = loader.getController();
            controller.header = header;
            controller.text = text;
            controller.finishInitialization(stage);
            Scene scene = new Scene(root, width, height);
            stage.setTitle(title);
            stage.setScene(scene);
            stage.show();
            //Play Alert/Notification sound
            Toolkit.getDefaultToolkit().beep();

            if (OSUtils.isWindows()) {
                try {
                    FXWinUtil.setDarkMode(stage, true);
                } catch (Exception ex) {
                    LOGGER.error("Failed to set theme of Windows title bar!", ex);
                }
            }

        } catch (Exception ex) {
            LOGGER.error("Failed to open alert popup!", ex);
        }

    }

    @FXML
    protected void initialize() {

    }

    protected void finishInitialization(@NotNull Stage stage) {
        this.stage = stage;
        this.headerLabel.setText(this.header);
        this.textLabel.setText(this.text);
    }

    @FXML
    protected void onOkayButtonClick() {
        if (this.stage != null) this.stage.close();
    }

}