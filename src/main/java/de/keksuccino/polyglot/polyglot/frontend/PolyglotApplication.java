package de.keksuccino.polyglot.polyglot.frontend;

import de.keksuccino.polyglot.polyglot.backend.Backend;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.jetbrains.annotations.Nullable;
import java.io.IOException;

public class PolyglotApplication extends javafx.application.Application {

    //TODO Zeit bis Timeout geworfen wird kürzer machen (20 sek)

    //TODO bei "Stop Process" click das unfertige File NICHT speichern

    //TODO Bei Safety override config option adden, um direkt mit "BLOCK_NONE" zu überschreiben und andere Level zu skippen (default: true)

    //TODO Safety Override nicht zurücksetzen, wenn Timeout (nicht reset() callen)

    @Nullable
    public static Stage stage;

    public static void main(String[] args) {

        Backend.init();

        launch();

    }

    @Override
    public void start(Stage stage) throws IOException {

        PolyglotApplication.stage = stage;
        FXMLLoader fxmlLoader = new FXMLLoader(PolyglotApplication.class.getResource("main-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 587, 845);

        stage.setTitle("Polyglot v" + Backend.VERSION);
        stage.setScene(scene);
        stage.show();

    }

}