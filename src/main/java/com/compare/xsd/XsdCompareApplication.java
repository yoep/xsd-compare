package com.compare.xsd;

import com.compare.xsd.ui.*;
import javafx.application.Application;
import javafx.stage.Stage;
import org.springframework.boot.Banner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

import java.io.File;

@SpringBootApplication
public class XsdCompareApplication extends Application {
    public static final String APP_DIR = System.getProperty("user.home") + File.separator + ".xsd-compare" + File.separator;
    public static ApplicationContext APPLICATION_CONTEXT;

    public static void main(String[] args) {
        SpringApplication application = new SpringApplication(XsdCompareApplication.class);
        application.setBannerMode(Banner.Mode.OFF);
        APPLICATION_CONTEXT = application.run(args);
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        ViewLoader loader = APPLICATION_CONTEXT.getBean(ViewLoader.class);
        ViewManager viewManager = APPLICATION_CONTEXT.getBean(ViewManagerImpl.class);

        loader.showPrimary(primaryStage, "main.fxml", ViewProperties.builder()
                .icon("logo_64.png")
                .title("XSD Compare")
                .centerOnScreen(true)
                .maximizable(true)
                .build());
        viewManager.setPolicy(ViewManagerPolicy.CLOSEABLE);
    }
}
