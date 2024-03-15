package de.keksuccino.polyglot.polyglot.frontend;

import de.keksuccino.polyglot.polyglot.backend.util.logger.LogHandler;
import de.keksuccino.polyglot.polyglot.backend.util.logger.SimpleLogger;
import de.keksuccino.polyglot.polyglot.frontend.controls.listview.NoSelectionModel;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.jetbrains.annotations.NotNull;
import java.util.ArrayList;
import java.util.List;

public class ConsoleViewController {

    private static final SimpleLogger LOGGER = LogHandler.getLogger();

    @FXML
    private ListView<Text> consoleListView;
    @FXML
    private CheckBox autoScrollingCheckBox;

    private int lineCount = 0;

    @FXML
    protected void initialize() {

        this.autoScrollingCheckBox.setSelected(true);

        //Make list view cells not selectable
        this.consoleListView.setSelectionModel(new NoSelectionModel<>());

    }

    protected void finishInitialization(@NotNull Stage stage) {

        Timeline tickTimeline = new Timeline(new KeyFrame(Duration.millis(100), actionEvent -> this.tick()));
        tickTimeline.setCycleCount(Animation.INDEFINITE);
        tickTimeline.play();

        //Stop the timeline when closing the window
        stage.setOnCloseRequest(event -> tickTimeline.stop());

    }

    protected void tick() {

        List<String> cachedHistory = new ArrayList<>(ConsoleViewMemory.LOGGING_HISTORY);;
        //Get all lines that aren't already in the ListView
        cachedHistory = cachedHistory.subList(this.lineCount, cachedHistory.size());

        //Add new lines to the ListView
        cachedHistory.forEach(s -> {
            s.lines().forEach(s1 -> {
                Text line = new Text(s1);
                if (s.contains("[ERROR]")) {
                    line.setFill(Color.web("#fa5a5a")); //red
                } else if (s.contains("[WARN]")) {
                    line.setFill(Color.web("#fcb94c")); //yellow
                } else {
                    line.setFill(Color.web("#e0e0e0")); //white
                }
                this.consoleListView.getItems().add(line);
            });
            this.lineCount++;
        });

        //Auto-scroll to the bottom if enabled
        if (!cachedHistory.isEmpty()) {
            if (this.autoScrollingCheckBox.isSelected()) {
                this.consoleListView.scrollTo(Math.max(0, this.consoleListView.getItems().size()-1));
            }
        }

    }

}