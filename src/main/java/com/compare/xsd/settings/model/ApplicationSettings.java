package com.compare.xsd.settings.model;

import lombok.*;

import java.util.Objects;

@EqualsAndHashCode(callSuper = false)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApplicationSettings extends AbstractSettings {
    public static final String COMPARE_SETTINGS_PROPERTY = "compareSettings";
    public static final String USER_INTERFACE_PROPERTY = "userInterface";

    @Builder.Default
    private CompareSettings compareSettings = CompareSettings.builder().build();
    @Builder.Default
    private UserInterface userInterface = UserInterface.builder().build();

    public void setCompareSettings(CompareSettings compareSettings) {
        if (Objects.equals(this.compareSettings, compareSettings))
            return;

        var oldValue = this.compareSettings;
        this.compareSettings = compareSettings;
        changes.firePropertyChange(COMPARE_SETTINGS_PROPERTY, oldValue, compareSettings);
    }

    public void setUserInterface(UserInterface userInterface) {
        if (Objects.equals(this.userInterface, userInterface))
            return;

        var oldValue = this.userInterface;
        this.userInterface = userInterface;
        changes.firePropertyChange(USER_INTERFACE_PROPERTY, oldValue, userInterface);
    }
}
