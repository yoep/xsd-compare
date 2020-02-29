package com.compare.xsd.controllers.components;

import com.compare.xsd.settings.SettingsService;
import com.compare.xsd.settings.model.UserInterface;
import com.compare.xsd.settings.model.UserSettings;
import javafx.beans.property.FloatProperty;
import javafx.beans.property.SimpleFloatProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ChoiceBox;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

import static java.util.Arrays.asList;

@Component
@RequiredArgsConstructor
public class SettingsGeneralComponent implements Initializable, SettingComponent {
    private final SettingsService settingsService;

    @FXML
    public ChoiceBox<ScaleItem> scaleFactor;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        initializeScaleFactor();
    }

    @Override
    public void apply(UserSettings userSettings) {
        userSettings.getUserInterface().setScale(scaleFactor.getSelectionModel().getSelectedItem().getScale().getValue());
    }

    private void initializeScaleFactor() {
        UserInterface userInterface = settingsService.getUserSettingsOrDefault().getUserInterface();
        List<ScaleItem> scaleItems = asList(
                new ScaleItem("50%", 0.5f),
                new ScaleItem("100%", 1.0f),
                new ScaleItem("150%", 1.5f),
                new ScaleItem("200%", 2.0f)
        );

        scaleFactor.getItems().addAll(scaleItems);
        scaleFactor.setValue(scaleItems.stream()
                .filter(e -> e.getScale().getValue().equals(userInterface.getScale()))
                .findFirst()
                .orElse(scaleItems.get(1)));
    }

    @Getter
    public static class ScaleItem {
        private StringProperty displayText = new SimpleStringProperty();
        private FloatProperty scale = new SimpleFloatProperty();

        public ScaleItem(String text, float scale) {
            this.displayText.setValue(text);
            this.scale.setValue(scale);
        }

        @Override
        public String toString() {
            return displayText.getValue();
        }
    }
}
