package com.compare.xsd.ui;

import javafx.stage.Stage;

/**
 * Defines that the controller is aware of the window events such as it being shown/closed.
 */
public interface WindowAware {
    /**
     * Is triggered when the window is shown.
     *
     * @param window The window that triggered the event.
     */
    void onShown(Stage window);

    /**
     * Is triggered when the window is closed.
     *
     * @param window The window that triggered the event.
     */
    void onClosed(Stage window);
}
