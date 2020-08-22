package com.compare.xsd.controllers.components;

import com.compare.xsd.settings.model.ApplicationSettings;

public interface SettingComponent {
    /**
     * Apply the configured settings to the given user settings.
     *
     * @param applicationSettings The current user settings to apply the configuration to.
     */
    void apply(ApplicationSettings applicationSettings);
}
