package com.compare.xsd.views.components;

import com.compare.xsd.settings.model.UserSettings;

public interface SettingComponent {
    /**
     * Apply the configured settings to the given user settings.
     *
     * @param userSettings The current user settings to apply the configuration to.
     */
    void apply(UserSettings userSettings);
}
