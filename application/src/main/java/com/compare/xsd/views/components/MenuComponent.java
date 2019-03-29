package com.compare.xsd.views.components;

import com.compare.xsd.loaders.ViewLoader;
import com.compare.xsd.views.ViewProperties;
import javafx.fxml.FXML;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;

@Log4j2
@Component
@RequiredArgsConstructor
public class MenuComponent {
    private final ViewLoader viewLoader;
    @Setter
    private Runnable onClearAll;
    @Setter
    private Runnable onLoadNextAvailableTree;
    @Setter
    private Runnable onExportToExcel;

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
        //TODO: implement
    }

    @FXML
    private void openHelpView() {
        viewLoader.showWindow("help.fxml", ViewProperties.builder()
                .title("Help")
                .maximizeDisabled(true)
                .build());
    }

    @FXML
    private void openBatchView() {
        viewLoader.showWindow("batch.fxml", ViewProperties.builder()
                .title("Batch comparison")
                .maximizeDisabled(true)
                .build());
    }
}
