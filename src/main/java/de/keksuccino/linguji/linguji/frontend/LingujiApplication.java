package de.keksuccino.linguji.linguji.frontend;

import de.keksuccino.linguji.linguji.backend.Backend;
import de.keksuccino.linguji.linguji.backend.util.logger.LogHandler;
import de.keksuccino.linguji.linguji.backend.util.logger.SimpleLogger;
import de.keksuccino.linguji.linguji.backend.util.os.OSUtils;
import de.keksuccino.linguji.linguji.frontend.util.os.windows.FXWinUtil;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.jetbrains.annotations.Nullable;
import java.io.IOException;

public class LingujiApplication extends javafx.application.Application {

    private static final SimpleLogger LOGGER = LogHandler.getLogger();

    @Nullable
    public static Stage stage;

    public static void main(String[] args) {

        Backend.init();

        ConsoleViewMemory.init();

        launch();

    }

    @Override
    public void start(Stage stage) throws IOException {

        LingujiApplication.stage = stage;

//        stage.setMinWidth(830);
//        stage.setMinHeight(870);

        FXMLLoader fxmlLoader = new FXMLLoader(LingujiApplication.class.getResource("main-view.fxml"));
        Parent parent = fxmlLoader.load();
        MainViewController controller = fxmlLoader.getController();
        controller.finishInitialization(stage, parent);
        Scene scene = new Scene(parent, 883, 860);

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