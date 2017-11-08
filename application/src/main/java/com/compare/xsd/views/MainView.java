package com.compare.xsd.views;

import com.compare.xsd.loaders.XsdLoader;
import com.compare.xsd.managers.ViewManager;
import com.compare.xsd.models.xsd.impl.XsdDocument;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableView;
import org.springframework.stereotype.Component;
import org.xml.sax.SAXException;

@Component
public class MainView {
    private final XsdLoader xsdLoader;
    private final ViewManager viewManager;

    public MainView(XsdLoader xsdLoader, ViewManager viewManager) {
        this.xsdLoader = xsdLoader;
        this.viewManager = viewManager;
    }

    public void loadLeftTree() throws SAXException {
        XsdDocument xsdDocument = xsdLoader.chooseAndLoad();

        TreeTableView leftTree = (TreeTableView) viewManager.getScene().lookup("#leftTree");
    }

    public void loadRightTree() {

    }
}
