package com.compare.xsd.ui;

import com.compare.xsd.settings.model.UserInterface;
import com.compare.xsd.ui.exceptions.MissingScaleAwarePropertyException;
import javafx.scene.Group;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Region;
import javafx.scene.transform.Scale;
import javafx.stage.Screen;
import javafx.stage.Window;

/**
 * Implementation of {@link ScaleAware} for scaling the scene during initialization.
 */
public abstract class ScaleAwareImpl implements ScaleAware {
    @Override
    public void scale(Scene scene, UserInterface userInterface) {
        if (scene == null) {
            throw new MissingScaleAwarePropertyException();
        }

        float scaleFactor = userInterface.getScale();
        Region root = (Region) scene.getRoot();
        Window window = scene.getWindow();

        //set initial window size
        window.setWidth(root.getPrefWidth() * scaleFactor);
        window.setHeight(root.getPrefHeight() * scaleFactor);

        //scale the scene by the given scale factor
        scene.setRoot(new Group(root));
        scene.widthProperty().addListener((observable, oldValue, newValue) -> root.setPrefWidth(newValue.doubleValue() * 1 / scaleFactor));
        scene.heightProperty().addListener((observable, oldValue, newValue) -> root.setPrefHeight(newValue.doubleValue() * 1 / scaleFactor));

        Scale scale = new Scale(scaleFactor, scaleFactor);
        scale.setPivotX(0);
        scale.setPivotY(0);
        root.getTransforms().setAll(scale);
    }
}
