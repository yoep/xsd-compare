package com.compare.xsd.views;

import com.compare.xsd.loaders.XsdLoader;
import com.compare.xsd.managers.PropertyViewManager;
import com.compare.xsd.managers.TreeViewManager;
import com.compare.xsd.managers.ViewManager;
import com.compare.xsd.model.xsd.XsdNode;
import com.compare.xsd.model.xsd.impl.XsdDocument;
import com.compare.xsd.renderers.PropertyViewRender;
import com.compare.xsd.renderers.TreeViewRender;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Cursor;
import javafx.scene.control.*;
import javafx.scene.input.DragEvent;
import javafx.scene.input.TransferMode;
import lombok.extern.java.Log;
import org.apache.commons.collections4.IteratorUtils;
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
    private final PropertyViewManager propertyViewManager;

    @FXML
    private TreeTableView<XsdNode> leftTree;

    @FXML
    private TreeTableView<XsdNode> rightTree;

    @FXML
    private SplitPane treeSplitPane;

    @FXML
    private SplitPane propertiesSplitPane;

    @FXML
    private TableView<PropertyViewRender.Property> leftProperties;

    @FXML
    private TableView<PropertyViewRender.Property> rightProperties;

    //region Constructors

    /**
     * Initialize a new instance of {@link MainView}.
     * This view contains the main screen of the application including the tree renders.
     *
     * @param xsdLoader           Set the XSD loader.
     * @param viewManager         Set the view manager.
     * @param treeViewManager     Set the tree view manager.
     * @param propertyViewManager Set the property manager.
     */
    public MainView(XsdLoader xsdLoader, ViewManager viewManager, TreeViewManager treeViewManager, PropertyViewManager propertyViewManager) {
        this.xsdLoader = xsdLoader;
        this.viewManager = viewManager;
        this.treeViewManager = treeViewManager;
        this.propertyViewManager = propertyViewManager;
    }

    //endregion

    //region Methods

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        PropertyViewRender leftProperties = new PropertyViewRender(this.leftProperties);
        PropertyViewRender rightProperties = new PropertyViewRender(this.rightProperties);
        TreeViewRender leftTreeRender = new TreeViewRender(leftTree, leftProperties);
        TreeViewRender rightTreeRender = new TreeViewRender(rightTree, rightProperties);

        this.treeViewManager.setLeftTreeRender(leftTreeRender);
        this.treeViewManager.setRightTreeRender(rightTreeRender);
        this.propertyViewManager.setLeftProperties(leftProperties);
        this.propertyViewManager.setRightProperties(rightProperties);
        this.viewManager.getStage().setOnShown(event -> {
            this.treeViewManager.synchronize();
            this.propertyViewManager.synchronize();
        });
        this.synchronizeDividers();
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

    /**
     * Clear all tree views.
     */
    public void clearAll() {
        treeViewManager.clearAll();
    }

    //endregion

    //region Functions

    private void compare() {
        XsdDocument originalDocument = treeViewManager.getLeftTreeRender().getDocument();
        XsdDocument newDocument = treeViewManager.getRightTreeRender().getDocument();

        originalDocument.compare(newDocument);
        treeViewManager.refresh(); // refresh tree views to reflect removed and added items
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

    private void synchronizeDividers() {
        SplitPane.Divider treeDivider = getFirstDivider(this.treeSplitPane.getDividers());
        SplitPane.Divider propertiesDivider = getFirstDivider(this.propertiesSplitPane.getDividers());

        treeDivider.positionProperty().addListener((observable, oldValue, newValue) -> {
            propertiesDivider.setPosition(newValue.doubleValue());
        });
        propertiesDivider.positionProperty().addListener((observable, oldValue, newValue) -> {
            treeDivider.setPosition(newValue.doubleValue());
        });
    }

    private SplitPane.Divider getFirstDivider(ObservableList<SplitPane.Divider> dividers) {
        return IteratorUtils.toList(dividers.iterator()).get(0);
    }

    //endregion
}
