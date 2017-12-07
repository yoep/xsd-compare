package com.compare.xsd.managers;

import javafx.scene.Scene;
import javafx.stage.Stage;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.util.HashMap;
import java.util.Map;

@Log4j2
@Getter
@ToString
@Component
public class ViewManager {
    public static final String PRIMARY_TITLE = "XSD Compare";
    public static final String BATCH_TITLE = "XSD Compare";

    private final Map<Stage, Scene> windows = new HashMap<>();

    private boolean primarySet;

    /**
     * Get the total amount of windows which are currently being shown.
     *
     * @return Returns the total amount of shown windows.
     */
    public int getTotalWindows() {
        return windows.size();
    }

    /**
     * Get the primary window of the application.
     *
     * @return Returns the primary window.
     * @throws WindowNotFoundException            Is thrown when the primary window couldn't be found back.
     * @throws PrimaryWindowNotAvailableException Is thrown when the primary window is not available yet.
     */
    public Stage getPrimaryWindow() throws WindowNotFoundException, PrimaryWindowNotAvailableException {
        if (primarySet) {
            return getWindow(PRIMARY_TITLE);
        } else {
            throw new PrimaryWindowNotAvailableException();
        }
    }

    /**
     * Get the window by the given name.
     *
     * @param name Set the name of the window.
     * @return Returns the found window.
     * @throws WindowNotFoundException Is thrown when the window with the given name couldn't be found.
     */
    public Stage getWindow(String name) throws WindowNotFoundException {
        for (Stage window : windows.keySet()) {
            if (window.getTitle().equals(name)) {
                return window;
            }
        }

        throw new WindowNotFoundException(name);
    }

    /**
     * Add a new opened window to the manager.
     *
     * @param window Set the new window.
     * @param view   Set the corresponding loaded view of the window.
     */
    public void addWindowView(Stage window, Scene view) {
        Assert.notNull(window, "window cannot be null");

        window.setOnCloseRequest(event -> this.windows.remove(event.getSource()));
        windows.put(window, view);
        log.debug("Currently showing " + getTotalWindows() + " window(s)");
    }

    /**
     * Add the primary window of the application.
     *
     * @param window Set the primary window.
     * @throws PrimaryWindowAlreadyPresentException Is thrown when the primary window is already present.
     */
    public void addPrimaryWindow(Stage window) throws PrimaryWindowAlreadyPresentException {
        Assert.notNull(window, "window cannot be null");

        if (!primarySet) {
            window.setTitle(PRIMARY_TITLE);
            addWindowView(window, null);
            primarySet = true;
            log.debug("Primary window is available");
        } else {
            throw new PrimaryWindowAlreadyPresentException();
        }
    }
}

