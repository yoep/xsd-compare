package com.compare.xsd.loaders;

import com.compare.xsd.managers.ViewManager;
import com.compare.xsd.views.ViewProperties;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import lombok.extern.java.Log;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.logging.Level;

@Log
@Component
public class ViewLoader {
    private static final String VIEW_DIRECTORY = "/views/";
    private static final String FONT_DIRECTORY = "/fonts/";

    private final ApplicationContext applicationContext;
    private final ViewManager viewManager;

    public ViewLoader(ApplicationContext applicationContext, ViewManager viewManager) {
        this.applicationContext = applicationContext;
        this.viewManager = viewManager;
    }

    @PostConstruct
    public void init() {
        loadFonts();
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
        FXMLLoader loader = new FXMLLoader(getClass().getResource(VIEW_DIRECTORY + view));

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

    /**
     * Load and show the given view.
     *
     * @param view Set the view to load and show.
     */
    public void show(String view) {
        Assert.hasText(view, "view cannot be empty");
        Scene scene = loadScene(view);

        viewManager.setScene(scene);
        viewManager.getStage().setScene(scene);
        viewManager.getStage().show();
    }

    /**
     * Show the given view in a new window.
     *
     * @param view       Set the view to load and show.
     * @param properties Set the properties of the window.
     */
    public void showWindow(String view, ViewProperties properties) {
        Assert.hasText(view, "view cannot be empty");
        Assert.notNull(properties, "properties cannot be null");
        Scene scene = loadScene(view);
        Stage stage = new Stage();

        if (properties.isMaximizeDisabled()) {
            stage.setResizable(false);
        }

        stage.setScene(scene);
        stage.setTitle(properties.getTitle());

        if (properties.isDialog()) {
            stage.showAndWait();
        } else {
            stage.show();
        }
    }

    private Scene loadScene(String view) {
        Parent loadedView = load(view);
        return new Scene(loadedView);
    }

    private void loadFonts() {
        Font.loadFont(getClass().getResource(FONT_DIRECTORY + "fontawesome-webfont.ttf").toExternalForm(), 10);
    }
}
