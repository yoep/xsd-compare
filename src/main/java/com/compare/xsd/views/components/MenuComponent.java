package com.compare.xsd.views.components;

import com.compare.xsd.messages.BatchMessage;
import com.github.spring.boot.javafx.text.LocaleText;
import com.github.spring.boot.javafx.view.ViewLoader;
import com.github.spring.boot.javafx.view.ViewProperties;
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
    private final LocaleText localeText;

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
                .resizable(false)
                .build());
    }

    @FXML
    private void openHelpView() {
        viewLoader.showWindow("help.fxml", ViewProperties.builder()
                .icon("logo_64.png")
                .title("Help")
                .resizable(false)
                .build());
    }

    @FXML
    private void openBatchView() {
        viewLoader.showWindow("batch.fxml", ViewProperties.builder()
                .icon("logo_64.png")
                .title(localeText.get(BatchMessage.TITLE))
                .resizable(false)
                .build());
    }
}
