package de.keksuccino.polyglot.polyglot.frontend;

import de.keksuccino.polyglot.polyglot.backend.Backend;
import de.keksuccino.polyglot.polyglot.backend.util.logger.LogHandler;
import de.keksuccino.polyglot.polyglot.backend.util.logger.SimpleLogger;
import de.keksuccino.polyglot.polyglot.backend.util.os.OSUtils;
import de.keksuccino.polyglot.polyglot.frontend.util.os.windows.FXWinUtil;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.jetbrains.annotations.Nullable;
import java.io.IOException;

public class PolyglotApplication extends javafx.application.Application {

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

        PolyglotApplication.stage = stage;

        stage.setMinWidth(830);
        stage.setMinHeight(830);

        FXMLLoader fxmlLoader = new FXMLLoader(PolyglotApplication.class.getResource("main-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 830, 826);

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