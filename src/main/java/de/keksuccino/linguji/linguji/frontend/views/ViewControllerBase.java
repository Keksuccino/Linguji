package de.keksuccino.linguji.linguji.frontend.views;

import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.AnchorPane;
import java.util.ArrayList;

public interface ViewControllerBase {

    default ArrayList<Node> getAllNodes(Parent root) {
        ArrayList<Node> nodes = new ArrayList<>();
        addAllDescendants(root, nodes);
        return nodes;
    }

    default void addAllDescendants(Parent parent, ArrayList<Node> nodes) {
        for (Node node : parent.getChildrenUnmodifiable()) {
            nodes.add(node);
            if (node instanceof Parent p) {
                addAllDescendants(p, nodes);
            }
            if (node instanceof ScrollPane p) {
                if (p.getContent() instanceof AnchorPane anchorPane) {
                    nodes.add(anchorPane);
                    addAllDescendants(anchorPane, nodes);
                }
            }
        }
    }

}
