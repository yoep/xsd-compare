package com.compare.xsd;

import javafx.stage.Stage;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ViewConfiguration {
    private Stage stage;

    /**
     * Initialize the view configuration.
     *
     * @param stage Set the application stage.
     */
    public void init(Stage stage) {
        this.stage = stage;
    }

    @Bean
    public Stage stage() {
        return this.stage;
    }
}
