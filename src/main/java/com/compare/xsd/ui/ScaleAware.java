package com.compare.xsd.ui;

import com.compare.xsd.settings.model.UserInterface;
import javafx.scene.Scene;

/**
 * Defines that a view controller is aware of the scale factor and the view is able to be scaled accordingly.
 */
public interface ScaleAware {
    /**
     * Scale the given scene according to the scale factor.
     *
     * @param scene         The current scene to use for scaling.
     * @param userInterface Set the user interface settings.
     */
    void scale(Scene scene, UserInterface userInterface);
}
