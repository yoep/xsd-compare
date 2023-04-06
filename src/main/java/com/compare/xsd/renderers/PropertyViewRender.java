package com.compare.xsd.renderers;

import com.compare.xsd.comparison.model.Change;
import com.compare.xsd.comparison.model.xsd.XsdNode;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.paint.Paint;
import javafx.util.Callback;
import lombok.*;

@EqualsAndHashCode
@ToString
@Getter
public class PropertyViewRender implements RenderView {
    private final TableView<Property> propertyView;

    private XsdNode node;

    //region Constructors

    /**
     * Initialize a new instance of {@link PropertyViewRender}.
     *
     * @param propertyView Set the view to use for rendering.
     */
    public PropertyViewRender(TableView<Property> propertyView) {
        this.propertyView = propertyView;

        init();
    }

    //endregion

    //region Implementation of RenderView

    @Override
    public boolean isRendering() {
        return node != null;
    }

    @Override
    public Node getNode() {
        return this.propertyView;
    }

    //endregion

    //region Methods

    /**
     * Render the properties of the given node.
     *
     * @param node Set the node to render.
     */
    public void render(XsdNode node) {
        ObservableList<Property> items = this.propertyView.getItems();
        Change modification = node.getChange() != null ? node.getChange() : new Change();

        items.clear();
        items.add(new Property("Name", node.getName(), modification.isNameChanged()));
        items.add(new Property("Type", node.getTypeName(), modification.isTypeChanged()));
        items.add(new Property("Type Namespace", node.getTypeNamespace(), modification.isNamespaceChanged()));
        items.add(new Property("Cardinality", node.getCardinality(), modification.isCardinalityChanged()));
        items.add(new Property("Fixed default", node.getFixedValue(), modification.isFixedDefaultChanged()));
        items.add(new Property("Pattern", node.getPattern(), modification.isPatternChanged()));
        items.add(new Property("Enumeration", node.getEnumeration(), modification.isEnumerationChanged()));
        items.add(new Property("Length", node.getLength(), modification.isLengthChanged()));
        items.add(new Property("Min. length", node.getMinLength(), modification.isMinLengthChanged()));
        items.add(new Property("Max. length", node.getMaxLength(), modification.isMaxLengthChanged()));
        items.add(new Property("Whitespace mode", node.getWhitespace(), modification.isWhitespaceChanged()));
        this.node = node;
    }

    /**
     * Clear the rendering of the properties.
     */
    public void clear() {
        this.node = null;
        this.propertyView.getItems().clear();
    }

    //endregion

    //region Functions

    private void init() {
        addPropertyColumn();
        addValueColumn();
        addContextMenu();
    }

    private void addContextMenu() {
        this.propertyView.setRowFactory(tableView -> {
            TableRow<Property> row = new TableRow<>();
            MenuItem copyValue = new MenuItem("Copy value to clipboard\t(Ctrl+C)");

            copyValue.setOnAction(event -> copyValueToClipboard(row.getItem()));

            row.contextMenuProperty().bind(Bindings.when(row.emptyProperty())
                    .then((ContextMenu) null)
                    .otherwise(new ContextMenu(copyValue)));

            return row;
        });
        this.propertyView.addEventHandler(KeyEvent.KEY_PRESSED, event -> {
            Property selectedItem = this.propertyView.getSelectionModel().getSelectedItem();

            if (selectedItem != null) {
                if (event.isControlDown() && event.getCode() == KeyCode.C) {
                    copyValueToClipboard(selectedItem);
                }
            }
        });
    }

    private void copyValueToClipboard(Property property) {
        ClipboardContent clipboardContent = new ClipboardContent();
        clipboardContent.putString(property.getValue().toString());
        Clipboard.getSystemClipboard().setContent(clipboardContent);
    }

    private void addPropertyColumn() {
        TableColumn<Property, String> property = createNewColumn("Property");

        property.setCellValueFactory(cellData -> new ReadOnlyStringWrapper(cellData.getValue().getProperty()));
    }

    private void addValueColumn() {
        TableColumn<Property, String> property = createNewColumn("Value");

        property.setCellValueFactory(cellData -> {
            Object value = cellData.getValue().getValue();

            return new ReadOnlyStringWrapper(value != null ? value.toString() : null);
        });
    }

    private TableColumn<Property, String> createNewColumn(String name) {
        TableColumn<Property, String> column = new TableColumn<>(name);

        column.prefWidthProperty().bind(propertyView.widthProperty().divide(2));
        column.setSortable(false);
        column.setEditable(false);

        column.setCellFactory(new Callback<TableColumn<Property, String>, TableCell<Property, String>>() {
            @Override
            public TableCell<Property, String> call(TableColumn<Property, String> param) {
                return new TableCell<Property, String>() {
                    @Override
                    protected void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);
                        setText(empty ? null : item);
                        Property property = (Property) this.getTableRow().getItem();

                        if (property != null && property.isModified()) {
                            setTextFill(Paint.valueOf("#be0000"));
                        } else {
                            setTextFill(Paint.valueOf("#000000"));
                        }
                    }
                };
            }
        });

        propertyView.getColumns().add(column);

        return column;
    }

    //endregion

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public class Property {
        private String property;
        private Object value;
        private boolean modified;
    }
}
