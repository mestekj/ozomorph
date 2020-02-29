/**
package org.example;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.GridPane;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Map extends GridPane {
    List<PositionMapNode> nodes;
    int width,height;

    @FXML
    private GridPane mapView;

    public Map(int width, int height) {
        this.width = width;
        this.height = height;

        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(
                "map.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);

        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }

        nodes = generateNodes();
    }

    private List<PositionMapNode> generateNodes() {
        List<PositionMapNode> nodes = new ArrayList<>();
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                var node = new PositionMapNode(i,j);
                nodes.add(node);
                mapView.add(node.guiNode,i,j);
            }
        }
        return nodes;
    }
}

 **/