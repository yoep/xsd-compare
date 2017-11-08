package com.compare.xsd.loaders;

import com.compare.xsd.models.xsd.XsdDocument;
import com.sun.org.apache.xerces.internal.impl.xs.XSComplexTypeDecl;
import com.sun.org.apache.xerces.internal.impl.xs.XSElementDecl;
import com.sun.org.apache.xerces.internal.impl.xs.XSLoaderImpl;
import com.sun.org.apache.xerces.internal.xs.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import javax.annotation.PostConstruct;
import java.io.File;

@Component
public class XsdLoader {
    private static final String EXTENSION = "*.xsd";
    private static final String EXTENSION_DESCRIPTION = "XML Schema Definition Language (XSD)";

    private final Stage stage;
    private final FileChooser fileChooser;

    public XsdLoader(Stage stage) {
        this.stage = stage;
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
        File file = fileChooser.showOpenDialog(stage);

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
        XSLoaderImpl loader = new XSLoaderImpl();
        XSModel model = loader.loadURI(file.getAbsolutePath());
        XSNamedMap elements = model.getComponents(XSConstants.ELEMENT_DECLARATION);

        for (int i = 0; i < elements.getLength(); i++) {
            XSObject item = elements.item(i);

            if (item instanceof XSElementDecl) {
                XSElementDecl element = (XSElementDecl) item;
                XSTypeDefinition typeDefinition = element.getTypeDefinition();

                if (typeDefinition.getTypeCategory() == XSTypeDefinition.COMPLEX_TYPE) {
                    XSComplexTypeDecl complexType = (XSComplexTypeDecl) typeDefinition;


                }
            }
        }


        return null;
    }
}
