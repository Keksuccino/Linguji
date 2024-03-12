package de.keksuccino.polyglot.polyglot.frontend;

import de.keksuccino.polyglot.polyglot.backend.Backend;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.jetbrains.annotations.Nullable;
import java.io.IOException;

public class PolyglotApplication extends javafx.application.Application {

    //TODO Zeit bis Timeout geworfen wird kürzer machen (20 sek) ( in getJsonFromPOST() )

    //TODO bei "Stop Process" click das unfertige File NICHT speichern

    //TODO Console View schreiben (printet alles von loggern, etc.)

    @Nullable
    public static Stage stage;

    public static void main(String[] args) {

        Backend.init();

        launch();

    }

    @Override
    public void start(Stage stage) throws IOException {

        PolyglotApplication.stage = stage;

        stage.setMinWidth(830);
        stage.setMinHeight(830);

        FXMLLoader fxmlLoader = new FXMLLoader(PolyglotApplication.class.getResource("main-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 830, 826);

        stage.setTitle("Polyglot v" + Backend.VERSION);
        stage.setScene(scene);
        stage.show();

    }

}