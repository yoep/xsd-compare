package com.compare.xsd.views;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import org.springframework.stereotype.Component;

@Component
public class ProgressView {
    @FXML
    public Label processText;
    @FXML
    public ProgressBar progress;
    @FXML
    public Label progressText;
}
