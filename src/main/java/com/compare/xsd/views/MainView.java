package com.compare.xsd.views;

import com.compare.xsd.comparison.PropertyViewManager;
import com.compare.xsd.comparison.TreeViewManager;
import com.compare.xsd.comparison.XsdComparer;
import com.compare.xsd.comparison.XsdLoader;
import com.compare.xsd.comparison.model.xsd.XsdNode;
import com.compare.xsd.comparison.model.xsd.impl.XsdDocument;
import com.compare.xsd.renderers.PropertyViewRender;
import com.compare.xsd.renderers.TreeViewRender;
import com.compare.xsd.renderers.TreeViewRenderBuilder;
import com.compare.xsd.settings.SettingsService;
import com.compare.xsd.settings.model.CompareSettings;
import com.compare.xsd.ui.ScaleAwareImpl;
import com.compare.xsd.ui.ViewManager;
import com.compare.xsd.ui.WindowAware;
import com.compare.xsd.ui.exceptions.ActionCancelledException;
import com.compare.xsd.ui.exceptions.PrimaryWindowNotAvailableException;
import com.compare.xsd.ui.exceptions.WindowNotFoundException;
import com.compare.xsd.views.components.MenuComponent;
import com.compare.xsd.writers.ExcelComparisonWriter;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Cursor;
import javafx.scene.control.*;
import javafx.scene.input.DragEvent;
import javafx.scene.input.TransferMode;
import javafx.stage.Stage;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.collections4.IteratorUtils;
import org.springframework.stereotype.Component;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

@Log4j2
@Component
@RequiredArgsConstructor
public class MainView extends ScaleAwareImpl implements Initializable, WindowAware {
    private final XsdLoader xsdLoader;
    private final ViewManager viewManager;
    private final TreeViewManager treeViewManager;
    private final PropertyViewManager propertyViewManager;
    private final ExcelComparisonWriter comparisonWriter;
    private final SettingsService settingsService;
    private final MenuComponent menuComponent;

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

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        CompareSettings compareSettings = settingsService.getUserSettingsOrDefault().getCompareSettings();

        PropertyViewRender leftProperties = new PropertyViewRender(this.leftProperties);
        PropertyViewRender rightProperties = new PropertyViewRender(this.rightProperties);
        TreeViewRender leftTreeRender = TreeViewRenderBuilder.builder()
                .treeView(leftTree)
                .propertyViewRender(leftProperties)
                .compareSettings(compareSettings)
                .build();
        TreeViewRender rightTreeRender = TreeViewRenderBuilder.builder()
                .treeView(rightTree)
                .propertyViewRender(rightProperties)
                .compareSettings(compareSettings)
                .build();

        this.treeViewManager.setLeftTreeRender(leftTreeRender);
        this.treeViewManager.setRightTreeRender(rightTreeRender);
        this.propertyViewManager.setLeftProperties(leftProperties);
        this.propertyViewManager.setRightProperties(rightProperties);
        this.menuComponent.setOnClearAll(this::clearAll);
        this.menuComponent.setOnExportToExcel(this::exportToExcel);
        this.menuComponent.setOnLoadNextAvailableTree(this::loadNextAvailableTree);
        this.synchronizeDividers();
    }

    @Override
    public void onShown(Stage window) {
        this.treeViewManager.synchronize();
        this.propertyViewManager.synchronize();
        window.setMaximized(true);
    }

    @Override
    public void onClosed(Stage window) {
        //no-op
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
        menuComponent.setComparisonEnabled(false);
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

    public void exportToExcel() {
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

    private void compare() {
        XsdDocument originalDocument = treeViewManager.getLeftTreeRender().getDocument();
        XsdDocument newDocument = treeViewManager.getRightTreeRender().getDocument();
        XsdComparer comparer = new XsdComparer(originalDocument, newDocument);

        setLoading();

        if (comparer.compare()) {
            this.propertyViewManager.clearAll();
            this.treeViewManager.refresh(); // refresh tree views to reflect removed and added items
            this.modificationsLabel.setText(comparer.toString());
            this.menuComponent.setComparisonEnabled(true);
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
}
