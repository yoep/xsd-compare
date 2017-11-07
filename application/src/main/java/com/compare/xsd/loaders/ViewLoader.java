package com.compare.xsd.loaders;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import lombok.extern.java.Log;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.io.IOException;
import java.util.logging.Level;

@Log
@Component
public class ViewLoader {
    private static final String DIRECTORY = "/views/";

    private final ApplicationContext applicationContext;

    public ViewLoader(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    /**
     * Load the given view.
     *
     * @param view Set the view name to load.
     * @return Returns the loaded view.
     * @throws ViewNotFoundException Is thrown when the given view file couldn't be found.
     */
    public Parent load(String view) throws ViewNotFoundException {
        Assert.hasText(view, "view cannot be empty");
        FXMLLoader loader = new FXMLLoader(getClass().getResource(DIRECTORY + view));

        loader.setControllerFactory(applicationContext::getBean);

        try {
            return loader.load();
        } catch (IllegalStateException ex) {
            throw new ViewNotFoundException(view, ex);
        } catch (IOException ex) {
            log.log(Level.SEVERE, "View '" + view + "' is invalid", ex);
        }

        return null;
    }
}
