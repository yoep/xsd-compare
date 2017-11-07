package com.compare.xsd.views;

import com.sun.org.apache.xerces.internal.impl.xs.XSLoaderImpl;
import com.sun.org.apache.xerces.internal.xs.XSConstants;
import com.sun.org.apache.xerces.internal.xs.XSModel;
import com.sun.org.apache.xerces.internal.xs.XSNamedMap;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.springframework.stereotype.Component;
import org.xml.sax.SAXException;

import java.io.File;

@Component
public class MainView {
    private final Stage stage;

    public MainView(Stage stage) {
        this.stage = stage;
    }

    public void loadLeftTree() throws SAXException {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setSelectedExtensionFilter(new FileChooser.ExtensionFilter("XSD file", "*.xsd"));
        fileChooser.setTitle("Select XSD to load");

        File file = fileChooser.showOpenDialog(stage);

        XSLoaderImpl loader = new XSLoaderImpl();
        XSModel model = loader.loadURI(file.getAbsolutePath());
        XSNamedMap elements = model.getComponents(XSConstants.ELEMENT_DECLARATION);


    }

    public void loadRightTree() {

    }
}
