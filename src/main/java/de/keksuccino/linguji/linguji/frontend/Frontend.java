package de.keksuccino.linguji.linguji.frontend;

import de.keksuccino.linguji.linguji.backend.Backend;
import de.keksuccino.linguji.linguji.frontend.views.AlertViewController;
import de.keksuccino.linguji.linguji.frontend.views.ConsoleViewMemory;
import javafx.stage.Stage;
import org.jetbrains.annotations.NotNull;

public class Frontend {

    public static Stage mainViewStage;

    public static void init() {

        Backend.init();

        ConsoleViewMemory.init();

    }

    public static void openAlert(@NotNull String title, @NotNull String header, @NotNull String text, int width, int height) {
        AlertViewController.openAlert(title, header, text, width, height, mainViewStage);
    }

    public static void openAlert(@NotNull String title, @NotNull String header, @NotNull String text) {
        openAlert(title, header, text, 568, 229);
    }

}
