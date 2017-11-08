package com.compare.xsd;

import com.compare.xsd.loaders.ViewLoader;
import com.compare.xsd.managers.ViewManager;
import javafx.application.Application;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

@SpringBootApplication
public class XsdCompareApplication extends Application {
    private static ApplicationContext applicationContext;

    public static void main(String[] args) {
        applicationContext = SpringApplication.run(XsdCompareApplication.class, args);
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        ViewManager viewManager = applicationContext.getBean(ViewManager.class);
        ViewLoader loader = applicationContext.getBean(ViewLoader.class);

        viewManager.setStage(primaryStage);
        primaryStage.setTitle("XSD Compare");
        loader.show("main.fxml");
    }
}
