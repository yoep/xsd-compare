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
    private final ApplicationContext applicationContext;

    public ViewLoader(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    public Parent load(String uri) {
        Assert.hasText(uri, "uri cannot be empty");
        FXMLLoader loader = new FXMLLoader(getClass().getResource(uri));

        loader.setControllerFactory(applicationContext::getBean);

        try {
            return loader.load();
        } catch (IllegalStateException ex) {
            log.log(Level.SEVERE, "View '" + uri + "' not found", ex);
        } catch (IOException ex) {
            log.log(Level.SEVERE, "View '" + uri + "' is invalid", ex);
        }

        return null;
    }
}
