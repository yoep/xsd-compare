package com.compare.xsd.managers;

import javafx.scene.Scene;
import javafx.stage.Stage;
import lombok.Data;
import org.springframework.stereotype.Component;

@Data
@Component
public class ViewManager {
    private Stage stage;
    private Scene scene;
}
