package com.compare.xsd.view.services;

import com.compare.xsd.settings.SettingsService;
import com.compare.xsd.settings.model.UserInterface;
import com.github.spring.boot.javafx.view.ViewLoader;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

@Slf4j
@Service
@RequiredArgsConstructor
public class InterfaceService {
    private final SettingsService settingsService;
    private final ViewLoader viewLoader;

    //region PostConstruct

    @PostConstruct
    private void init() {
        initializeScale();
    }

    void initializeScale() {
        var userInterface = settingsService.getSettings().getUserInterface();

        onScaleChanged(userInterface.getScale());
        userInterface.addListener(event -> {
            if (event.getPropertyName().equals(UserInterface.SCALE_PROPERTY)) {
                onScaleChanged((float) event.getNewValue());
            }
        });
    }

    //endregion

    //region Functions

    private void onScaleChanged(float scale) {
        viewLoader.setScale(scale);
    }

    //endregion
}
