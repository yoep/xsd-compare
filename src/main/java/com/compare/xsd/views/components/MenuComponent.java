package com.compare.xsd.views.components;

import com.compare.xsd.ui.UIText;
import com.compare.xsd.ui.ViewLoader;
import com.compare.xsd.ui.ViewProperties;
import com.compare.xsd.ui.lang.BatchMessage;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;

@Log4j2
@Component
@RequiredArgsConstructor
public class MenuComponent {
    private final ViewLoader viewLoader;
    private final UIText uiText;

    @Setter
    private Runnable onClearAll;
    @Setter
    private Runnable onLoadNextAvailableTree;
    @Setter
    private Runnable onExportToExcel;

    @FXML
    private Button exportComparisonButton;

    public void setComparisonEnabled(boolean enabled) {
        exportComparisonButton.setDisable(!enabled);
    }

    @FXML
    private void clearAll() {
        if (onClearAll != null)
            onClearAll.run();
    }

    @FXML
    private void loadNextAvailableTree() {
        if (onLoadNextAvailableTree != null)
            onLoadNextAvailableTree.run();
    }

    @FXML
    private void exportToExcel() {
        if (onExportToExcel != null)
            onExportToExcel.run();
    }

    @FXML
    private void openSettingsView() {
        viewLoader.showWindow("settings.fxml", ViewProperties.builder()
                .icon("logo_64.png")
                .title("Settings")
                .maximizable(false)
                .build());
    }

    @FXML
    private void openHelpView() {
        viewLoader.showWindow("help.fxml", ViewProperties.builder()
                .icon("logo_64.png")
                .title("Help")
                .maximizable(false)
                .build());
    }

    @FXML
    private void openBatchView() {
        viewLoader.showWindow("batch.fxml", ViewProperties.builder()
                .icon("logo_64.png")
                .title(uiText.get(BatchMessage.TITLE))
                .maximizable(false)
                .build());
    }
}
