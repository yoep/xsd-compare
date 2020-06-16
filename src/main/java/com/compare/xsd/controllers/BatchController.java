package com.compare.xsd.controllers;

import com.compare.xsd.messages.BatchMessage;
import com.github.spring.boot.javafx.text.LocaleText;
import com.github.spring.boot.javafx.ui.scale.ScaleAwareImpl;
import com.github.spring.boot.javafx.view.ViewManager;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.io.File;
import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

@Slf4j
@Component
@RequiredArgsConstructor
public class BatchController extends ScaleAwareImpl implements Initializable {
    private final ViewManager viewManager;
    private final LocaleText localeText;

    @FXML
    private Button cancelButton;
    @FXML
    private Button executeButton;

    @FXML
    private TextField originalDirectoryInput;
    @FXML
    private TextField newDirectoryInput;

    //region Methods

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        originalDirectoryInput.textProperty().addListener((observable, oldValue, newValue) -> directoryChanged());
        newDirectoryInput.textProperty().addListener((observable, oldValue, newValue) -> directoryChanged());
    }

    /**
     * Open a selection dialog window for the original directory.
     */
    public void selectOriginalDirectory() {
        selectDirectory(originalDirectoryInput);
    }

    /**
     * Open a selection dialog window for the new directory.
     */
    public void selectNewDirectory() {
        selectDirectory(newDirectoryInput);
    }

    /**
     * Invocation method which identifies if the directory inputs have been changed.
     */
    public void directoryChanged() {
        if (StringUtils.isNotEmpty(originalDirectoryInput.getText()) && StringUtils.isNotEmpty(newDirectoryInput.getText())) {
            executeButton.setDisable(false);
        } else {
            executeButton.setDisable(true);
        }
    }

    public void execute() {

    }

    /**
     * Closes the window.
     */
    public void close() {
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
    }

    //endregion

    //region Functions

    private void selectDirectory(TextField directoryField) {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        Optional<Stage> stage = viewManager.getStage(localeText.get(BatchMessage.TITLE));

        if (!stage.isPresent()) {
            log.warn("Failed to open directory selector, batch stage is missing");
            return;
        }

        File file = directoryChooser.showDialog(stage.get());

        if (file != null) {
            directoryField.setText(file.getAbsolutePath());
        }
    }

    //endregion
}
