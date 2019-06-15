package com.compare.xsd.renderers;

import com.compare.xsd.XsdCompareApplication;
import com.compare.xsd.comparison.model.xsd.XsdNode;
import com.compare.xsd.settings.model.CompareSettings;
import com.compare.xsd.ui.UIText;
import javafx.scene.control.TreeTableView;
import org.springframework.util.Assert;

public class TreeViewRenderBuilder {
    private TreeTableView<XsdNode> treeView;
    private PropertyViewRender propertyViewRender;
    private CompareSettings compareSettings;

    private TreeViewRenderBuilder() {
    }

    public static TreeViewRenderBuilder builder() {
        return new TreeViewRenderBuilder();
    }

    /**
     * The tree view to render the XSD document on.
     *
     * @param treeView The tree view to use for rendering.
     * @return Returns this builder.
     */
    public TreeViewRenderBuilder treeView(TreeTableView<XsdNode> treeView) {
        this.treeView = treeView;
        return this;
    }

    /**
     * The property view to render the XSD element information on.
     *
     * @param propertyViewRender The property rendered.
     * @return Returns this builder.
     */
    public TreeViewRenderBuilder propertyViewRender(PropertyViewRender propertyViewRender) {
        this.propertyViewRender = propertyViewRender;
        return this;
    }

    /**
     * The compare settings observable to use for visible columns.
     *
     * @param compareSettings The compare settings observable.
     * @return Returns this builder.
     */
    public TreeViewRenderBuilder compareSettings(CompareSettings compareSettings) {
        this.compareSettings = compareSettings;
        return this;
    }

    /**
     * Build the TreeViewRender instance.
     *
     * @return Returns the TreeViewRender instance.
     */
    public TreeViewRender build() {
        Assert.notNull(treeView, "treeView cannot be null");
        Assert.notNull(propertyViewRender, "propertyViewRender cannot be null");
        Assert.notNull(compareSettings, "compareSettings cannot be null");
        TreeViewRender treeViewRender = new TreeViewRender(treeView, propertyViewRender, XsdCompareApplication.APPLICATION_CONTEXT.getBean(UIText.class));
        treeViewRender.setVisibleColumns(compareSettings.getShownColumns());

        compareSettings.addObserver((o, arg) -> {
            treeViewRender.setVisibleColumns(compareSettings.getShownColumns());
            treeViewRender.initialize();
        });

        treeViewRender.initialize();
        return treeViewRender;
    }
}
