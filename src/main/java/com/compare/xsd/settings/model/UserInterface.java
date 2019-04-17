package com.compare.xsd.settings.model;

import lombok.*;

import java.util.Observable;

@EqualsAndHashCode(callSuper = false)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserInterface extends Observable {
    @Builder.Default
    private boolean maximized = true;
    @Builder.Default
    private float width = 800f;
    @Builder.Default
    private float height = 600f;
    @Builder.Default
    private float scale = 1f;

    public void setScale(float scale) {
        this.scale = scale;
        this.setChanged();
        this.notifyObservers();
    }
}
