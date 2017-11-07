package com.compare.xsd;

import javafx.application.Application;
import javafx.stage.Stage;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class XsdCompareApplication extends Application {
    public static void main(String[] args) {
        SpringApplication.run(XsdCompareApplication.class, args);
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {

    }
}
