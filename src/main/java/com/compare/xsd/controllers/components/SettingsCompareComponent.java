package com.compare.xsd.controllers.components;

import com.compare.xsd.settings.SettingsService;
import com.compare.xsd.settings.model.CompareColumns;
import com.compare.xsd.settings.model.CompareSettings;
import com.compare.xsd.settings.model.UserSettings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ListView;
import javafx.scene.control.cell.CheckBoxListCell;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.net.URL;
import java.util.Arrays;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class SettingsCompareComponent implements Initializable, SettingComponent {
    private final SettingsService settingsService;

    @FXML
    public ListView<ShownColumnItem> shownColumns;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        initializeListView();
    }

    @Override
    public void apply(UserSettings userSettings) {
        CompareSettings compareSettings = userSettings.getCompareSettings();

        compareSettings.setShownColumns(shownColumns.getItems().stream()
                .filter(e -> e.getEnabled().getValue())
                .map(e -> e.getName().getValue())
                .map(CompareColumns::valueOf)
                .collect(Collectors.toList()));
    }

    private void initializeListView() {
        CompareSettings compareSettings = settingsService.getUserSettingsOrDefault().getCompareSettings();

        shownColumns.setCellFactory(CheckBoxListCell.forListView(shownColumnItem -> shownColumnItem.enabled));
        shownColumns.getItems().addAll(Arrays.stream(CompareColumns.values())
                .map(e -> new ShownColumnItem(e.toString(), compareSettings.getShownColumns().contains(e)))
                .collect(Collectors.toList()));
    }

    @Data
    public static class ShownColumnItem {
        private StringProperty name = new SimpleStringProperty();
        private BooleanProperty enabled = new SimpleBooleanProperty();

        ShownColumnItem(String name, boolean enabled) {
            this.name.setValue(name);
            this.enabled.setValue(enabled);
        }

        @Override
        public String toString() {
            return name.getValue();
        }
    }
}
