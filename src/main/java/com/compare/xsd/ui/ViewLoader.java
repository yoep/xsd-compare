package com.compare.xsd.ui;

import com.compare.xsd.settings.SettingsService;
import com.compare.xsd.settings.model.UserInterface;
import com.compare.xsd.settings.model.UserSettings;
import com.compare.xsd.ui.exceptions.PrimaryWindowNotAvailableException;
import com.compare.xsd.ui.exceptions.ViewNotFoundException;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.text.Font;
import javafx.stage.Modality;
import javafx.stage.Screen;
import javafx.stage.Stage;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import javax.annotation.PostConstruct;
import java.io.IOException;

@Log4j2
@Component
@RequiredArgsConstructor
public class ViewLoader {
    public static final String VIEW_DIRECTORY = "/views/";
    private static final String FONT_DIRECTORY = "/fonts/";
    private static final String IMAGE_DIRECTORY = "/images/";

    private final SettingsService userSettingsService;
    private final ApplicationContext applicationContext;
    private final ViewManager viewManager;
    private final UIText uiText;

    @PostConstruct
    public void init() {
        loadFonts();
    }

    /**
     * Load and show the given view.
     *
     * @param view Set the view to load and show.
     */
    public void show(String view, ViewProperties properties) {
        Assert.hasText(view, "view cannot be empty");
        Assert.notNull(properties, "properties cannot be null");

        try {
            showScene(viewManager.getPrimaryWindow(), view, properties);
        } catch (PrimaryWindowNotAvailableException ex) {
            log.error(ex.getMessage(), ex);
        }
    }

    /**
     * Show the primary scene on the given primary window.
     *
     * @param window     Set the window.
     * @param view       Set the view scene to load.
     * @param properties Set the view properties.
     */
    public void showPrimary(Stage window, String view, ViewProperties properties) {
        Assert.notNull(window, "window cannot be empty");
        Assert.hasText(view, "view cannot be empty");
        Assert.notNull(properties, "properties cannot be null");
        showScene(window, view, properties);
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
        Platform.runLater(() -> showScene(new Stage(), view, properties));
    }

    /**
     * Load the given view.
     *
     * @param view Set the view name to load.
     * @return Returns the loaded view.
     * @throws ViewNotFoundException Is thrown when the given view file couldn't be found.
     */
    private SceneInfo load(String view) throws ViewNotFoundException {
        Assert.hasText(view, "view cannot be empty");
        FXMLLoader loader = new FXMLLoader(getClass().getResource(VIEW_DIRECTORY + view));

        loader.setControllerFactory(applicationContext::getBean);
        loader.setResources(uiText.getResourceBundle());

        try {
            Scene scene = new Scene(loader.load());
            Object controller = loader.getController();

            return new SceneInfo(scene, controller);
        } catch (IllegalStateException ex) {
            throw new ViewNotFoundException(view, ex);
        } catch (IOException ex) {
            log.error("View '" + view + "' is invalid", ex);
        }

        return null;
    }

    /**
     * Show the given scene filename in the given window with the given properties.
     *
     * @param window     Set the window to show the view in.
     * @param view       Set the view to load and render.
     * @param properties Set the view properties.
     */
    private void showScene(Stage window, String view, ViewProperties properties) {
        SceneInfo sceneInfo = load(view);

        if (sceneInfo != null) {
            Scene scene = sceneInfo.getScene();
            Object controller = sceneInfo.getController();

            window.setScene(scene);
            viewManager.addWindowView(window, scene);

            if (controller instanceof ScaleAware) {
                initWindowScale(scene, (ScaleAware) controller);
            }
            if (controller instanceof SizeAware) {
                initWindowSize(scene, (SizeAware) controller);
            }
            if (controller instanceof WindowAware) {
                initWindowEvents(scene, (WindowAware) controller);
            }

            setWindowViewProperties(window, properties);

            if (properties.isDialog()) {
                window.initModality(Modality.APPLICATION_MODAL);
                window.showAndWait();
            } else {
                window.show();
            }
        } else {
            log.warn("Unable to show view " + view + " in window " + window);
        }
    }

    private void setWindowViewProperties(Stage window, ViewProperties properties) {
        if (!properties.isMaximizable()) {
            window.setResizable(false);
        }
        if (StringUtils.isNoneEmpty(properties.getIcon())) {
            window.getIcons().add(loadWindowIcon(properties.getIcon()));
        }
        if (properties.isCenterOnScreen()) {
            centerOnScreen(window);
        }

        window.setTitle(properties.getTitle());
    }

    /**
     * Center the given window on the screen.
     *
     * @param window Set the window to center.
     */
    private void centerOnScreen(Stage window) {
        Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();

        window.setX((screenBounds.getWidth() - window.getWidth()) / 2);
        window.setY((screenBounds.getHeight() - window.getHeight()) / 2);
    }

    private Image loadWindowIcon(String iconName) {
        return new Image(getClass().getResourceAsStream(IMAGE_DIRECTORY + iconName));
    }

    private void loadFonts() {
        Font.loadFont(getClass().getResource(FONT_DIRECTORY + "fontawesome-webfont.ttf").toExternalForm(), 10);
    }

    private void initWindowScale(Scene scene, ScaleAware controller) {
        controller.scale(scene, userSettingsService.getUserSettings()
                .map(UserSettings::getUserInterface)
                .orElse(UserInterface.builder().build()));
    }

    private void initWindowSize(Scene scene, SizeAware controller) {
        Stage window = (Stage) scene.getWindow();
        controller.setInitialSize(window);
        window.widthProperty().addListener((observable, oldValue, newValue) -> {
            if (window.isShowing()) {
                controller.onSizeChange(newValue, window.getHeight(), window.isMaximized());
            }
        });
        window.heightProperty().addListener((observable, oldValue, newValue) -> {
            if (window.isShowing()) {
                controller.onSizeChange(window.getWidth(), newValue, window.isMaximized());
            }
        });
        window.maximizedProperty().addListener(((observable, oldValue, newValue) -> {
            if (window.isShowing()) {
                controller.onSizeChange(window.getWidth(), window.getHeight(), newValue);
            }
        }));
    }

    private void initWindowEvents(Scene scene, WindowAware controller) {
        final Stage window = (Stage) scene.getWindow();

        window.setOnShown(event -> controller.onShown(window));
        window.setOnCloseRequest(event -> controller.onClosed(window));
    }

    @Getter
    @AllArgsConstructor
    private class SceneInfo {
        private Scene scene;
        private Object controller;
    }
}
