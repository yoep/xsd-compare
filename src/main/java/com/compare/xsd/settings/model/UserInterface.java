package com.compare.xsd.settings.model;

import lombok.*;

import java.util.Objects;

@EqualsAndHashCode(callSuper = false)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserInterface extends AbstractSettings {
    public static final String MAXIMIZED_PROPERTY = "maximized";
    public static final String WIDTH_PROPERTY = "width";
    public static final String HEIGHT_PROPERTY = "height";
    public static final String SCALE_PROPERTY = "scale";

    @Builder.Default
    private boolean maximized = true;
    @Builder.Default
    private float width = 800f;
    @Builder.Default
    private float height = 600f;
    @Builder.Default
    private float scale = 1f;

    public void setMaximized(boolean maximized) {
        if (Objects.equals(this.maximized, maximized))
            return;

        var oldValue = this.maximized;
        this.maximized = maximized;
        changes.firePropertyChange(MAXIMIZED_PROPERTY, oldValue, maximized);
    }

    public void setWidth(float width) {
        if (Objects.equals(this.width, width))
            return;

        var oldValue = this.width;
        this.width = width;
        changes.firePropertyChange(WIDTH_PROPERTY, oldValue, width);
    }

    public void setHeight(float height) {
        if (Objects.equals(this.height, height))
            return;

        var oldValue = this.height;
        this.height = height;
        changes.firePropertyChange(HEIGHT_PROPERTY, oldValue, height);
    }

    public void setScale(float scale) {
        if (Objects.equals(this.scale, scale))
            return;

        var oldValue = this.scale;
        this.scale = scale;
        changes.firePropertyChange(SCALE_PROPERTY, oldValue, scale);
    }
}
