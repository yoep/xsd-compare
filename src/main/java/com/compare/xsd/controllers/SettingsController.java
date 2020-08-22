package com.compare.xsd.controllers;

import com.compare.xsd.controllers.components.SettingComponent;
import com.compare.xsd.settings.SettingsService;
import com.github.spring.boot.javafx.ui.scale.ScaleAwareImpl;
import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import javafx.stage.Stage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class SettingsController extends ScaleAwareImpl {
    private final List<SettingComponent> settingComponents;
    private final SettingsService settingsService;

    public void apply(ActionEvent event) {
        var applicationSettings = settingsService.getSettings();

        settingComponents.forEach(e -> e.apply(applicationSettings));
        settingsService.save(applicationSettings);

        close(event);
    }

    public void close(ActionEvent event) {
        Button button = (Button) event.getSource();
        Stage stage = (Stage) button.getScene().getWindow();
        stage.close();
    }
}
