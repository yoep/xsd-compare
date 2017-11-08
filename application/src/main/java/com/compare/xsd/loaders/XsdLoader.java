package com.compare.xsd.loaders;

import com.compare.xsd.managers.ViewManager;
import com.compare.xsd.models.xsd.impl.XsdDocument;
import javafx.stage.FileChooser;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import javax.annotation.PostConstruct;
import java.io.File;

@Component
public class XsdLoader {
    private static final String EXTENSION = "*.xsd";
    private static final String EXTENSION_DESCRIPTION = "XML Schema Definition Language (XSD)";

    private final ViewManager viewManager;
    private final FileChooser fileChooser;

    public XsdLoader(ViewManager viewManager) {
        this.viewManager = viewManager;
        this.fileChooser = new FileChooser();
    }

    @PostConstruct
    public void init() {
        FileChooser.ExtensionFilter extensionFilter = new FileChooser.ExtensionFilter(EXTENSION_DESCRIPTION, EXTENSION);

        this.fileChooser.getExtensionFilters().add(extensionFilter);
    }

    /**
     * Open a {@link FileChooser} and load the selected XSD file.
     *
     * @return Returns the loaded XSD file.
     */
    public XsdDocument chooseAndLoad() {
        File file = fileChooser.showOpenDialog(viewManager.getStage());

        return load(file);
    }

    /**
     * Load the given XSD file into a {@link XsdDocument}.
     *
     * @param file Set the XSD file to load.
     * @return Returns the loaded {@link XsdDocument}.
     */
    public XsdDocument load(File file) {
        Assert.notNull(file, "file cannot be null");

        return new XsdDocument(file);
    }
}
