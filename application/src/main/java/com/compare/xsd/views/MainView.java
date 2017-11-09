package com.compare.xsd.views;

import com.compare.xsd.loaders.XsdLoader;
import com.compare.xsd.managers.TreeViewManager;
import com.compare.xsd.managers.ViewManager;
import com.compare.xsd.models.xsd.XsdNode;
import com.compare.xsd.models.xsd.impl.XsdDocument;
import com.compare.xsd.renderers.TreeViewRender;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Cursor;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TreeTableView;
import javafx.scene.input.DragEvent;
import javafx.scene.input.TransferMode;
import lombok.extern.java.Log;
import org.springframework.stereotype.Component;
import org.xml.sax.SAXException;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Level;

@Log
@Component
public class MainView implements Initializable {
    private final XsdLoader xsdLoader;
    private final ViewManager viewManager;
    private final TreeViewManager treeViewManager;

    @FXML
    private TreeTableView<XsdNode> leftTree;

    @FXML
    private TreeTableView<XsdNode> rightTree;

    /**
     * Initialize a new instance of {@link MainView}.
     * This view contains the main screen of the application including the tree renders.
     *
     * @param xsdLoader       Set the XSD loader.
     * @param viewManager     Set the view manager.
     * @param treeViewManager Set the tree view manager.
     */
    public MainView(XsdLoader xsdLoader, ViewManager viewManager, TreeViewManager treeViewManager) {
        this.xsdLoader = xsdLoader;
        this.viewManager = viewManager;
        this.treeViewManager = treeViewManager;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        this.treeViewManager.setLeftTreeRender(new TreeViewRender(leftTree));
        this.treeViewManager.setRightTreeRender(new TreeViewRender(rightTree));
        this.viewManager.getStage().setOnShown(event -> this.treeViewManager.synchronize());
    }

    public void loadLeftTree() throws SAXException {
        loadTree(this.treeViewManager.getLeftTreeRender());
    }

    public void loadRightTree() {
        loadTree(this.treeViewManager.getRightTreeRender());
    }

    /**
     * Handle a drag over event invoked on one of the tree views.
     *
     * @param event Set the event.
     */
    public void onDragOver(DragEvent event) {
        event.acceptTransferModes(TransferMode.ANY);
    }

    /**
     * Handle a drag enter event invoked on one of the tree views.
     *
     * @param event Set the event.
     */
    public void onDragEntered(DragEvent event) {
        viewManager.getScene().setCursor(Cursor.HAND);
        event.consume();
    }

    /**
     * Handle a drag exited event invoked on one of the tree views.
     *
     * @param event Set the event.
     */
    public void onDragExited(DragEvent event) {
        viewManager.getScene().setCursor(Cursor.DEFAULT);
        event.consume();
    }

    /**
     * Handle a drag dropped event invoked on one of the tree views.
     *
     * @param event Set the event.
     */
    public void onDragDropped(DragEvent event) {
        if (event.getSource() instanceof TreeTableView) {
            TreeTableView<XsdNode> source = (TreeTableView) event.getSource();

            loadTree(treeViewManager.getRenderer(source), event.getDragboard().getFiles().get(0));

            event.consume();
        } else {
            log.severe("Unknown drag dropped source " + event.getSource().getClass());
        }
    }

    private void compare() {
        XsdDocument originalDocument = treeViewManager.getLeftTreeRender().getDocument();
        XsdDocument newDocument = treeViewManager.getRightTreeRender().getDocument();

        originalDocument.compare(newDocument);
    }

    private void loadTree(TreeViewRender treeViewRender) {
        loadTree(treeViewRender, null);
    }

    private void loadTree(TreeViewRender treeViewRender, File file) {
        XsdDocument xsdDocument;

        try {
            if (file == null) {
                xsdDocument = xsdLoader.chooseAndLoad();
            } else {
                xsdDocument = xsdLoader.load(file);
            }

            if (xsdDocument != null) {
                treeViewRender.render(xsdDocument);
            }

            if (treeViewManager.getLeftTreeRender().isRendering() && treeViewManager.getRightTreeRender().isRendering()) {
                compare();
            }
        } catch (Exception ex) {
            log.log(Level.SEVERE, ex.getMessage(), ex);
            new Alert(Alert.AlertType.ERROR, "We are sorry, but an unexpected error occurred.\n" + ex.getMessage(), ButtonType.OK).show();
        }
    }
}
