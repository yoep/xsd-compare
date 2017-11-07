package com.compare.xsd.views;

import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.springframework.stereotype.Component;

@Component
public class MainView {
    private final Stage stage;

    public MainView(Stage stage) {
        this.stage = stage;
    }

    public void loadLeftTree() {
        FileChooser fileChooser = new FileChooser();

        fileChooser.showOpenDialog(stage);
    }

    public void loadRightTree() {

    }
}
