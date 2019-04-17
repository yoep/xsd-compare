package com.compare.xsd.views;

import com.compare.xsd.ui.ScaleAwareImpl;
import com.compare.xsd.ui.UIText;
import com.compare.xsd.ui.ViewManager;
import com.compare.xsd.ui.exceptions.WindowNotFoundException;
import com.compare.xsd.ui.lang.BatchMessage;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

@Log4j2
@Component
@RequiredArgsConstructor
public class BatchView extends ScaleAwareImpl implements Initializable {
    private final ViewManager viewManager;
    private final UIText uiText;

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
        File file;

        try {
            file = directoryChooser.showDialog(viewManager.getWindow(uiText.get(BatchMessage.TITLE)));

            if (file != null) {
                directoryField.setText(file.getAbsolutePath());
            }
        } catch (WindowNotFoundException ex) {
            log.error(ex.getMessage(), ex);
        }
    }

    //endregion
}
