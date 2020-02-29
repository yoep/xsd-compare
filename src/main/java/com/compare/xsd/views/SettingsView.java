package com.compare.xsd.views;

import com.compare.xsd.settings.SettingsService;
import com.compare.xsd.settings.model.UserSettings;
import com.compare.xsd.views.components.SettingComponent;
import com.github.spring.boot.javafx.ui.scale.ScaleAwareImpl;
import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import javafx.stage.Stage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class SettingsView extends ScaleAwareImpl {
    private final List<SettingComponent> settingComponents;
    private final SettingsService settingsService;

    public void apply(ActionEvent event) {
        UserSettings userSettings = settingsService.getUserSettingsOrDefault();

        settingComponents.forEach(e -> e.apply(userSettings));
        settingsService.save(userSettings);

        close(event);
    }

    public void close(ActionEvent event) {
        Button button = (Button) event.getSource();
        Stage stage = (Stage) button.getScene().getWindow();
        stage.close();
    }
}
