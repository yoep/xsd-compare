package com.compare.xsd.views;

import com.compare.xsd.loaders.XsdLoader;
import com.compare.xsd.managers.ViewManager;
import com.compare.xsd.models.xsd.XsdNode;
import com.compare.xsd.models.xsd.impl.XsdDocument;
import com.compare.xsd.renderers.TreeViewRender;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TreeTableView;
import lombok.extern.java.Log;
import org.springframework.stereotype.Component;
import org.xml.sax.SAXException;

import java.util.logging.Level;

@Log
@Component
public class MainView {
    private static final String NODE_SELECTOR_LEFT_TREE = "#leftTree";
    private static final String NODE_SELECTOR_RIGHT_TREE = "#rightTree";

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
        loadTree(NODE_SELECTOR_LEFT_TREE, leftTreeViewRender);
    }

    public void loadRightTree() {
        loadTree(NODE_SELECTOR_RIGHT_TREE, rightTreeViewRender);
    }

    private void loadTree(String selector, TreeViewRender treeViewRender) {
        try {
            XsdDocument xsdDocument = xsdLoader.chooseAndLoad();

            if (xsdDocument != null) {
                if (treeViewRender == null) {
                    Node node = viewManager.getScene().lookup(selector);

                    if (node != null && node instanceof TreeTableView) {
                        treeViewRender = new TreeViewRender((TreeTableView<XsdNode>) node);
                    } else {
                        throw new ViewComponentNotFoundException(selector);
                    }
                }

                treeViewRender.render(xsdDocument);
            }
        } catch (Exception ex) {
            log.log(Level.SEVERE, ex.getMessage(), ex);
            new Alert(Alert.AlertType.ERROR, "We are sorry, but an unexpected error occurred.\n" + ex.getMessage(), ButtonType.OK).show();
        }
    }
}
