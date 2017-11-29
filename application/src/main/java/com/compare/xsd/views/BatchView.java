package com.compare.xsd.views;

import com.compare.xsd.managers.ViewManager;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.stage.DirectoryChooser;
import org.springframework.stereotype.Component;

@Component
public class BatchView {
    private final ViewManager viewManager;

    @FXML
    private TextField originalDirectoryInput;
    @FXML
    private TextField newDirectoryInput;

    public BatchView(ViewManager viewManager) {
        this.viewManager = viewManager;
    }

    public void selectOriginalDirectory() {
        DirectoryChooser directoryChooser = new DirectoryChooser();

        directoryChooser.showDialog(viewManager.getStage());
    }
}
