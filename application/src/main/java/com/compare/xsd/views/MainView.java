package com.compare.xsd.views;

import com.compare.xsd.compare.XsdComparer;
import com.compare.xsd.loaders.ViewLoader;
import com.compare.xsd.loaders.XsdLoader;
import com.compare.xsd.managers.*;
import com.compare.xsd.model.xsd.XsdNode;
import com.compare.xsd.model.xsd.impl.XsdDocument;
import com.compare.xsd.renderers.PropertyViewRender;
import com.compare.xsd.renderers.TreeViewRender;
import com.compare.xsd.ui.ActionCancelledException;
import com.compare.xsd.writers.ExcelComparisonWriter;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Cursor;
import javafx.scene.control.*;
import javafx.scene.input.DragEvent;
import javafx.scene.input.TransferMode;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.collections4.IteratorUtils;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

@Log4j2
@Component
public class MainView implements Initializable {
    private final XsdLoader xsdLoader;
    private final ViewLoader viewLoader;
    private final ViewManager viewManager;
    private final TreeViewManager treeViewManager;
    private final PropertyViewManager propertyViewManager;
    private final ExcelComparisonWriter comparisonWriter;

    private XsdComparer comparer;

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

    @FXML
    private Label modificationsLabel;

    @FXML
    private Label progressBarLabel;
    @FXML
    private ProgressBar progressBar;

    @FXML
    private Button exportComparisonButton;

    //region Constructors

    /**
     * Initialize a new instance of {@link MainView}.
     * This view contains the main screen of the application including the tree renders.
     *
     * @param xsdLoader           Set the XSD loader.
     * @param viewLoader          Set the view loader.
     * @param viewManager         Set the view manager.
     * @param treeViewManager     Set the tree view manager.
     * @param propertyViewManager Set the property manager.
     * @param comparisonWriter    Set the Excel writer.
     */
    public MainView(XsdLoader xsdLoader,
                    ViewLoader viewLoader,
                    ViewManager viewManager,
                    TreeViewManager treeViewManager,
                    PropertyViewManager propertyViewManager,
                    ExcelComparisonWriter comparisonWriter) {
        this.xsdLoader = xsdLoader;
        this.viewLoader = viewLoader;
        this.viewManager = viewManager;
        this.treeViewManager = treeViewManager;
        this.propertyViewManager = propertyViewManager;
        this.comparisonWriter = comparisonWriter;
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
        this.synchronizeDividers();

        try {
            this.viewManager.getPrimaryWindow().setOnShown(event -> {
                this.treeViewManager.synchronize();
                this.propertyViewManager.synchronize();
            });
        } catch (WindowNotFoundException | PrimaryWindowNotAvailableException ex) {
            log.error(ex.getMessage(), ex);
            throw new RuntimeException(ex);
        }
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
    public void onDragEntered(DragEvent event) throws WindowNotFoundException, PrimaryWindowNotAvailableException {
        viewManager.getPrimaryWindow().getScene().setCursor(Cursor.HAND);
        event.consume();
    }

    /**
     * Handle a drag exited event invoked on one of the tree views.
     *
     * @param event Set the event.
     */
    public void onDragExited(DragEvent event) throws WindowNotFoundException, PrimaryWindowNotAvailableException {
        viewManager.getPrimaryWindow().getScene().setCursor(Cursor.DEFAULT);
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
            log.error("Unknown drag dropped source " + event.getSource().getClass());
        }
    }

    /**
     * Clear all tree views.
     */
    public void clearAll() {
        treeViewManager.clearAll();
        propertyViewManager.clearAll();
        modificationsLabel.setText("");
        exportComparisonButton.setDisable(true);
        log.debug("Cleared treeViewManager and propertyViewManager");
    }

    /**
     * Open and load the given file in the next available tree.
     */
    public void loadNextAvailableTree() {
        if (treeViewManager.getLeftTreeRender().isRendering()) {
            loadTree(treeViewManager.getRightTreeRender());
        } else {
            loadTree(treeViewManager.getLeftTreeRender());
        }
    }

    public void exportToExcel() throws IOException {
        if (comparer != null) {
            try {
                setWriting();

                comparisonWriter.save(comparer, comparisonWriter.showSaveDialog())
                        .thenAccept(state -> {
                            Platform.runLater(() -> {
                                if (state) {
                                    setLoadingDone();
                                } else {
                                    setLoadingFailed();
                                }
                            });
                        });
            } catch (ActionCancelledException ex) {
                setLoadingDone();
            } catch (Exception ex) {
                log.error(ex.getMessage(), ex);
                setLoadingFailed();
            }
        }
    }

    public void openSettingsView() {
        //TODO: implement
    }

    public void openHelpView() {
        viewLoader.showWindow("help.fxml", ViewProperties.builder()
                .title("Help")
                .maximizeDisabled(true)
                .build());
    }

    public void openBatchView() {
        viewLoader.showWindow("batch.fxml", ViewProperties.builder()
                .title("Batch comparison")
                .maximizeDisabled(true)
                .build());
    }

    //endregion

    //region Functions

    private void compare() {
        XsdDocument originalDocument = treeViewManager.getLeftTreeRender().getDocument();
        XsdDocument newDocument = treeViewManager.getRightTreeRender().getDocument();
        XsdComparer comparer = new XsdComparer(originalDocument, newDocument);

        setLoading();

        if (comparer.compare()) {
            this.propertyViewManager.clearAll();
            this.treeViewManager.refresh(); // refresh tree views to reflect removed and added items
            this.modificationsLabel.setText(comparer.toString());
            this.exportComparisonButton.setDisable(false);
            this.comparer = comparer;

            setLoadingDone();
        } else {
            setLoadingFailed();
        }
    }

    private void loadTree(TreeViewRender treeViewRender) {
        loadTree(treeViewRender, null);
    }

    private void loadTree(TreeViewRender treeViewRender, File file) {
        XsdDocument xsdDocument;

        try {
            setLoading();

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

            setLoadingDone();
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
            setLoadingFailed();
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

    private void setLoading() {
        progressBarLabel.setText("Loading...");
        progressBar.setProgress(-1);
        progressBar.setStyle("-fx-accent: dodgerblue");
    }

    private void setWriting() {
        progressBarLabel.setText("Writing...");
        progressBar.setProgress(-1);
        progressBar.setStyle("-fx-accent: dodgerblue");
    }

    private void setLoadingDone() {
        progressBarLabel.setText("Done");
        progressBar.setProgress(1);
        progressBar.setStyle("-fx-accent: limegreen");
    }

    private void setLoadingFailed() {
        progressBarLabel.setText("Failed");
        progressBar.setProgress(1);
        progressBar.setStyle("-fx-accent: red");
    }

    //endregion
}
