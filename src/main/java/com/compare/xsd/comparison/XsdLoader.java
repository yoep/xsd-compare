package com.compare.xsd.comparison;

import com.compare.xsd.comparison.model.xsd.impl.XsdDocument;
import com.github.spring.boot.javafx.view.ViewManager;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import javax.annotation.PostConstruct;
import java.io.File;
import java.util.Optional;

@Log4j2
@Component
public class XsdLoader {
    private static final String EXTENSION = "*.xsd";
    private static final String EXTENSION_DESCRIPTION = "XML Schema Definition Language (XSD)";

    private final ViewManager viewManager;
    private final FileChooser fileChooser;

    /**
     * Initialize a new instance of {@link XsdLoader}.
     *
     * @param viewManager Set the view manager.
     */
    public XsdLoader(ViewManager viewManager) {
        this.viewManager = viewManager;
        this.fileChooser = new FileChooser();
    }

    @PostConstruct
    public void init() {
        FileChooser.ExtensionFilter extensionFilter = new FileChooser.ExtensionFilter(EXTENSION_DESCRIPTION, EXTENSION);

        this.fileChooser.getExtensionFilters().add(extensionFilter);
        this.fileChooser.setInitialDirectory(new File(System.getProperty("user.dir")));
    }

    /**
     * Open a {@link FileChooser} and load the selected XSD file.
     *
     * @return Returns the loaded XSD file.
     */
    public XsdDocument chooseAndLoad() {
        Optional<Stage> stage = viewManager.getPrimaryStage();

        if (!stage.isPresent()) {
            log.warn("Unable to show XSD loader, primary stage is missing");
            return null;
        }

        File file = fileChooser.showOpenDialog(stage.get());

        if (file != null) {
            this.fileChooser.setInitialDirectory(file.getParentFile());
            return load(file);
        } else {
            return null;
        }
    }

    /**
     * Load the given XSD file into a {@link XsdDocument}.
     *
     * @param file Set the XSD file to load.
     * @return Returns the loaded {@link XsdDocument}.
     */
    public XsdDocument load(File file) {
        Assert.notNull(file, "file cannot be null");

        log.debug("Loading xsd file " + file);
        return new XsdDocument(file);
    }
}
