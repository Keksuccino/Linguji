package de.keksuccino.linguji.linguji.frontend;

import de.keksuccino.linguji.linguji.backend.Backend;
import de.keksuccino.linguji.linguji.backend.lib.logger.LogHandler;
import de.keksuccino.linguji.linguji.backend.lib.logger.SimpleLogger;
import de.keksuccino.linguji.linguji.backend.lib.os.OSUtils;
import de.keksuccino.linguji.linguji.frontend.util.os.windows.FXWinUtil;
import de.keksuccino.linguji.linguji.frontend.views.MainViewController;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;

public class LingujiApplication extends javafx.application.Application {

    private static final SimpleLogger LOGGER = LogHandler.getLogger();

    public static void main(String[] args) {

        Frontend.init();

        launch();

    }

    @Override
    public void start(Stage stage) throws IOException {

        Frontend.mainViewStage = stage;

        FXMLLoader fxmlLoader = new FXMLLoader(MainViewController.class.getResource("main-view.fxml"));
        Parent parent = fxmlLoader.load();
        MainViewController controller = fxmlLoader.getController();
        controller.finishInitialization(stage, parent);
        Scene scene = new Scene(parent, 883, 847);

        stage.setTitle("Linguji v" + Backend.VERSION);
        stage.setScene(scene);
        stage.show();

        if (OSUtils.isWindows()) {
            try {
                FXWinUtil.setDarkMode(stage, true);
            } catch (Exception ex) {
                LOGGER.error("Failed to set theme of Windows title bar!", ex);
            }
        }

    }

}