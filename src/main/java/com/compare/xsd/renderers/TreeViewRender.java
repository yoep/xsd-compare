package com.compare.xsd.renderers;

import com.compare.xsd.comparison.model.xsd.XsdNode;
import com.compare.xsd.comparison.model.xsd.impl.XsdDocument;
import com.compare.xsd.messages.MenuMessage;
import com.compare.xsd.settings.model.CompareColumns;
import com.github.spring.boot.javafx.text.LocaleText;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.util.Assert;

import java.util.List;
import java.util.Optional;

/**
 * Renders a {@link XsdDocument} within a {@link TreeTableView}.
 */
@Data
@RequiredArgsConstructor
public class TreeViewRender implements RenderView {
    private final TreeTableView<XsdNode> treeView;
    private final PropertyViewRender propertyViewRender;
    private final LocaleText localeText;
    private List<CompareColumns> visibleColumns;

    private XsdDocument document;

    //region Implementation of RenderView

    /**
     * Get if a document is currently being rendered.
     *
     * @return Returns true if a document is being rendered, else false.
     */
    public boolean isRendering() {
        return this.document != null;
    }

    @Override
    public Node getNode() {
        return this.treeView;
    }

    //endregion

    //region Methods

    /**
     * Refresh the tree view.
     */
    public void refresh() {
        if (isRendering()) {
            this.render(this.document);
        }
    }

    /**
     * Render the given {@link XsdDocument} in the tree view.
     *
     * @param xsdDocument Set the xsd document.
     */
    public void render(XsdDocument xsdDocument) {
        Assert.notNull(xsdDocument, "xsdDocument cannot be null");
        TreeItem<XsdNode> rootItem = new TreeItem<>(xsdDocument);

        renderChildren(xsdDocument.getNodes(), rootItem);

        rootItem.setExpanded(true);
        this.treeView.setRoot(rootItem);
        this.document = xsdDocument;
    }

    public void clear() {
        this.document = null;
        this.treeView.setRoot(null);
    }

    void initialize() {
        //clear all columns
        treeView.getColumns().clear();

        //create default columns
        addNameColumn();
        addColorColumn();

        //create additional columns
        if (visibleColumns.contains(CompareColumns.TYPE))
            addTypeColumn();
        if (visibleColumns.contains(CompareColumns.CARDINALITY))
            addCardinalityColumn();
        if (visibleColumns.contains(CompareColumns.FIXED_VALUE))
            addFixedValueColumn();
        if (visibleColumns.contains(CompareColumns.PATTERN))
            addPatternColumn();
        if (visibleColumns.contains(CompareColumns.LENGTH))
            addLengthColumn();

        //selection and context menu
        addSelectionListener();
        addContextMenu();
    }

    //endregion

    //region Functions

    private void addContextMenu() {
        this.treeView.setRowFactory(treeView -> {
            TreeTableRow<XsdNode> row = new TreeTableRow<>();
            MenuItem copyName = new MenuItem(localeText.get(MenuMessage.COPY_NAME));
            MenuItem copyXPath = new MenuItem(localeText.get(MenuMessage.COPY_XPATH));
            MenuItem copyXml = new MenuItem(localeText.get(MenuMessage.COPY_XML));

            copyName.setOnAction(event -> copyNameToClipboard(row.getItem()));
            copyXPath.setOnAction(event -> copyXPathToClipboard(row.getItem()));
            copyXml.setOnAction(event -> copyXmlToClipboard(row.getItem()));

            row.contextMenuProperty().bind(Bindings.when(row.emptyProperty())
                    .then((ContextMenu) null)
                    .otherwise(new ContextMenu(copyName, copyXPath, copyXml)));

            return row;
        });
        this.treeView.addEventHandler(KeyEvent.KEY_PRESSED, event -> {
            TreeItem<XsdNode> selectedItem = this.treeView.getSelectionModel().getSelectedItem();

            if (selectedItem != null) {
                if (event.isControlDown() && event.getCode() == KeyCode.C) {
                    copyNameToClipboard(selectedItem.getValue());
                }
                if (event.isControlDown() && event.isAltDown() && event.getCode() == KeyCode.C) {
                    copyXPathToClipboard(selectedItem.getValue());
                }
                if (event.isControlDown() && event.isShiftDown() && event.getCode() == KeyCode.C) {
                    copyXmlToClipboard(selectedItem.getValue());
                }
            }
        });
    }

    private void copyNameToClipboard(XsdNode item) {
        copyTextToClipboard(item.getName());
    }

    private void copyXPathToClipboard(XsdNode item) {
        copyTextToClipboard(item.getXPath());
    }

    private void copyXmlToClipboard(XsdNode item) {
        copyTextToClipboard(item.getXml());
    }

    private void copyTextToClipboard(String text) {
        ClipboardContent clipboardContent = new ClipboardContent();
        clipboardContent.putString(text);
        Clipboard.getSystemClipboard().setContent(clipboardContent);
    }

    private void renderChildren(List<XsdNode> elements, TreeItem<XsdNode> parent) {
        for (XsdNode element : elements) {
            TreeItem<XsdNode> elementTree = new TreeItem<>(element);

            if (CollectionUtils.isNotEmpty(element.getNodes())) {
                renderChildren(element.getNodes(), elementTree);
            }

            elementTree.setExpanded(true);
            parent.getChildren().add(elementTree);
        }
    }

    private void addNameColumn() {
        TreeTableColumn<XsdNode, String> column = createNewColumn("Name", 350);

        column.setPrefWidth(-1);
        column.setMinWidth(-1);
        column.setMaxWidth(5000);

        column.setCellValueFactory(cellData -> {
            TreeItem<XsdNode> treeItem = cellData.getValue();
            XsdNode node = treeItem.getValue();

            return new ReadOnlyStringWrapper(node.getName());
        });

        column.setCellFactory(treeColumn -> new TreeTableCell<XsdNode, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                TreeItem<XsdNode> treeItem = getTreeTableRow().getTreeItem();
                XsdNode xsdNode = treeItem != null ? treeItem.getValue() : null;

                setText(empty ? null : item);
                setTooltip(empty ? null : new Tooltip(item));
                setGraphic(empty || xsdNode == null ? null : new ImageView(xsdNode.getIcon()));
            }
        });
    }

    private void addTypeColumn() {
        TreeTableColumn<XsdNode, String> column = createNewColumn("Type", 80);

        column.setCellValueFactory(cellData -> {
            TreeItem<XsdNode> treeItem = cellData.getValue();
            XsdNode node = treeItem.getValue();

            return new ReadOnlyStringWrapper(node.getTypeName());
        });

        column.setCellFactory(treeColumn -> createTextCellFactoryWithTooltip());
    }

    private void addCardinalityColumn() {
        TreeTableColumn<XsdNode, String> column = createNewColumn("Cardinality", 50);

        column.setCellValueFactory(cellData -> {
            TreeItem<XsdNode> treeItem = cellData.getValue();
            XsdNode node = treeItem.getValue();

            return new ReadOnlyStringWrapper(node.getCardinality());
        });

        column.setCellFactory(treeColumn -> new TreeTableCell<XsdNode, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);

                setText(empty ? null : item);
                setAlignment(Pos.CENTER);
            }
        });
    }

    private void addFixedValueColumn() {
        TreeTableColumn<XsdNode, String> column = createNewColumn("Fixed value", 80);

        column.setCellValueFactory(cellData -> {
            TreeItem<XsdNode> treeItem = cellData.getValue();
            XsdNode node = treeItem.getValue();

            return new ReadOnlyStringWrapper(node.getFixedValue());
        });

        column.setCellFactory(treeColumn -> createTextCellFactoryWithTooltip());
    }

    private void addPatternColumn() {
        TreeTableColumn<XsdNode, String> column = createNewColumn("Pattern", 125);

        column.setCellValueFactory(cellData -> {
            TreeItem<XsdNode> treeItem = cellData.getValue();
            XsdNode node = treeItem.getValue();

            return new ReadOnlyStringWrapper(node.getPattern());
        });

        column.setCellFactory(treeColumn -> createTextCellFactoryWithTooltip());
    }

    private void addLengthColumn() {
        TreeTableColumn<XsdNode, String> column = createNewColumn("Length", 50);

        column.setCellValueFactory(cellData -> {
            TreeItem<XsdNode> treeItem = cellData.getValue();
            XsdNode node = treeItem.getValue();

            return new ReadOnlyStringWrapper(Optional.ofNullable(node.getLength())
                    .map(String::valueOf)
                    .orElse(null));
        });

        column.setCellFactory(treeColumn -> createTextCellFactoryWithTooltip());
    }

    private void addColorColumn() {
        TreeTableColumn<XsdNode, String> column = createNewColumn("", 20);

        column.setCellFactory(treeColumn -> new TreeTableCell<XsdNode, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                TreeItem<XsdNode> treeItem = getTreeTableRow().getTreeItem();
                XsdNode xsdNode = treeItem != null ? treeItem.getValue() : null;
                Image modificationColor = xsdNode != null ? xsdNode.getModificationColor() : null;

                setGraphic(modificationColor == null ? null : new ImageView(modificationColor));
                setAlignment(Pos.CENTER);
            }
        });
    }

    private TreeTableColumn<XsdNode, String> createNewColumn(String name, double width) {
        TreeTableColumn<XsdNode, String> column = new TreeTableColumn<>(name);

        column.setPrefWidth(width);
        column.setMinWidth(width);
        column.setMaxWidth(width);
        column.setSortable(false);
        column.setEditable(false);

        treeView.getColumns().add(column);

        return column;
    }

    private TreeTableCell<XsdNode, String> createTextCellFactoryWithTooltip() {
        return new TreeTableCell<XsdNode, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);

                setText(empty ? null : item);
                setTooltip(empty ? null : new Tooltip(item));
            }
        };
    }

    private void addSelectionListener() {
        this.treeView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                this.propertyViewRender.render(newValue.getValue());
            }
        });
    }

    //endregion
}
