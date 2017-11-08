package com.compare.xsd.views;

import com.compare.xsd.loaders.XsdLoader;
import com.compare.xsd.managers.ViewManager;
import com.compare.xsd.models.xsd.XsdNode;
import com.compare.xsd.models.xsd.impl.XsdDocument;
import com.compare.xsd.renderers.TreeViewRender;
import javafx.scene.control.TreeTableView;
import org.springframework.stereotype.Component;
import org.xml.sax.SAXException;

@Component
public class MainView {
    private final XsdLoader xsdLoader;
    private final ViewManager viewManager;

    private TreeViewRender leftTreeViewRender;
    private TreeViewRender rightTreeViewRender;

    /**
     * Initialize a new instance of {@link MainView}.
     * This view contains the main screen of the application including the tree renders.
     *
     * @param xsdLoader   Set the XSD loader.
     * @param viewManager Set the view manager.
     */
    public MainView(XsdLoader xsdLoader, ViewManager viewManager) {
        this.xsdLoader = xsdLoader;
        this.viewManager = viewManager;
    }

    public void loadLeftTree() throws SAXException {
        XsdDocument xsdDocument = xsdLoader.chooseAndLoad();

        if (leftTreeViewRender == null) {
            leftTreeViewRender = new TreeViewRender((TreeTableView<XsdNode>) viewManager.getScene().lookup("#leftTree"));
        }

        leftTreeViewRender.render(xsdDocument);
    }

    public void loadRightTree() {

    }
}
