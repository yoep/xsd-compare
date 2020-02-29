package com.compare.xsd.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import org.springframework.stereotype.Component;

@Component
public class ProgressController {
    @FXML
    public Label processText;
    @FXML
    public ProgressBar progress;
    @FXML
    public Label progressText;
}
