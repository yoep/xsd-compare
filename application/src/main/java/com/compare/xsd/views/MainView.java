package com.compare.xsd.views;

import com.compare.xsd.loaders.XsdLoader;
import com.compare.xsd.models.xsd.XsdDocument;
import org.springframework.stereotype.Component;
import org.xml.sax.SAXException;

@Component
public class MainView {
    private final XsdLoader xsdLoader;

    public MainView(XsdLoader xsdLoader) {
        this.xsdLoader = xsdLoader;
    }

    public void loadLeftTree() throws SAXException {
        XsdDocument xsdDocument = xsdLoader.chooseAndLoad();
    }

    public void loadRightTree() {

    }
}
