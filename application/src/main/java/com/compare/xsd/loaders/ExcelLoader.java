package com.compare.xsd.loaders;

import com.compare.xsd.managers.ViewManager;
import javafx.stage.FileChooser;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.File;

@Component
public class ExcelLoader {
    private static final String EXTENSION_DESCRIPTION = "Excel file";
    private static final String EXTENSION = "*.xlsx";

    private final ViewManager viewManager;
    private final FileChooser chooser;

    /**
     * Initialize a new instance of {@link ExcelLoader}.
     *
     * @param viewManager Set the view manager.
     */
    public ExcelLoader(ViewManager viewManager) {
        this.viewManager = viewManager;
        this.chooser = new FileChooser();
    }

    @PostConstruct
    public void init() {
        FileChooser.ExtensionFilter extensionFilter = new FileChooser.ExtensionFilter(EXTENSION_DESCRIPTION, EXTENSION);

        chooser.setTitle("Select save location");
        chooser.getExtensionFilters().add(extensionFilter);
    }

    public File showSaveDialog() {
        return chooser.showSaveDialog(viewManager.getStage());
    }
}
