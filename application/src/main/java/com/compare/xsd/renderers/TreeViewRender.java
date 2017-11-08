package com.compare.xsd.renderers;

import com.compare.xsd.models.xsd.XsdNode;
import com.compare.xsd.models.xsd.impl.XsdDocument;
import com.compare.xsd.models.xsd.impl.XsdElement;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableCell;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableView;
import javafx.scene.image.ImageView;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.util.Assert;

import java.util.List;

/**
 * Renders a {@link XsdDocument} within a {@link TreeTableView}.
 */
public class TreeViewRender {
    private final TreeTableView<XsdNode> treeView;

    //region Constructors

    /**
     * Initialize a new instance of {@link TreeViewRender}.
     *
     * @param treeView Set the tree view to use for the rendering.
     */
    public TreeViewRender(TreeTableView<XsdNode> treeView) {
        Assert.notNull(treeView, "treeView cannot be null");
        this.treeView = treeView;

        init();
    }

    //endregion

    //region Methods

    /**
     * Render the given {@link XsdDocument} in the tree view.
     *
     * @param xsdDocument Set the xsd document.
     */
    public void render(XsdDocument xsdDocument) {
        Assert.notNull(xsdDocument, "xsdDocument cannot be null");
        TreeItem<XsdNode> rootItem = new TreeItem<>(xsdDocument);

        renderChildren(xsdDocument.getElements(), rootItem);

        rootItem.setExpanded(true);
        treeView.setRoot(rootItem);
    }

    //endregion

    //region Functions

    private void init() {
        treeView.getColumns().clear();

        addNameColumn();
        addTypeColumn();
        addCardinalityColumn();
    }

    private void renderChildren(List<XsdElement> elements, TreeItem<XsdNode> parent) {
        for (XsdElement element : elements) {
            TreeItem<XsdNode> elementTree = new TreeItem<>(element);

            if (CollectionUtils.isNotEmpty(element.getChildElements())) {
                renderChildren(element.getChildElements(), elementTree);
            }

            elementTree.setExpanded(true);
            parent.getChildren().add(elementTree);
        }
    }

    private void addNameColumn() {
        TreeTableColumn<XsdNode, String> column = initNewColumn("Name", 350);

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
                setGraphic(empty || xsdNode == null ? null : new ImageView(xsdNode.getIcon()));
            }
        });
    }

    private void addTypeColumn() {
        TreeTableColumn<XsdNode, String> column = initNewColumn("Type", 80);

        column.setCellValueFactory(cellData -> {
            TreeItem<XsdNode> treeItem = cellData.getValue();
            XsdNode node = treeItem.getValue();

            return new ReadOnlyStringWrapper(node.getType());
        });
    }

    private void addCardinalityColumn() {
        TreeTableColumn<XsdNode, String> column = initNewColumn("Type", 50);

        column.setCellValueFactory(cellData -> {
            TreeItem<XsdNode> treeItem = cellData.getValue();
            XsdNode node = treeItem.getValue();

            return new ReadOnlyStringWrapper(node.getCardinality());
        });
    }

    private TreeTableColumn<XsdNode, String> initNewColumn(String name, double width) {
        TreeTableColumn<XsdNode, String> column = new TreeTableColumn<>(name);

        column.setPrefWidth(width);
        column.setSortable(false);
        column.setEditable(false);

        treeView.getColumns().add(column);

        return column;
    }

    //endregion
}
