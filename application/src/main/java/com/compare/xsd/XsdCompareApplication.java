package com.compare.xsd;

import com.compare.xsd.loaders.ViewLoader;
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
        ViewConfiguration viewConfiguration = applicationContext.getBean(ViewConfiguration.class);
        ViewLoader loader = applicationContext.getBean(ViewLoader.class);
        Parent root = loader.load("/views/main.fxml");
        Scene scene = new Scene(root);

        viewConfiguration.init(primaryStage);

        primaryStage.setScene(scene);
        primaryStage.show();
    }
}
