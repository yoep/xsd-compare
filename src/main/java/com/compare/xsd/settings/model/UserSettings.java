package com.compare.xsd.settings.model;

import lombok.*;

import java.util.Observable;

@EqualsAndHashCode(callSuper = false)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserSettings extends Observable {
    @Builder.Default
    private CompareSettings compareSettings = CompareSettings.builder().build();
    @Builder.Default
    private UserInterface userInterface = UserInterface.builder().build();
}
