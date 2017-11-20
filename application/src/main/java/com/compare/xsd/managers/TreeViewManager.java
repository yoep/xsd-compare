package com.compare.xsd.managers;

import com.compare.xsd.model.xsd.XsdNode;
import com.compare.xsd.renderers.TreeViewRender;
import javafx.scene.control.TreeTableView;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

@Log4j2
@Data
@EqualsAndHashCode(callSuper = false)
@Component
public class TreeViewManager extends AbstractScrollBarSynchronizeManager {
    private TreeViewRender leftTreeRender;
    private TreeViewRender rightTreeRender;

    private boolean isSelecting;

    //region Methods

    /**
     * Get the renderer for the given node.
     *
     * @param node Set the node.
     * @return Returns the renderer of the node.
     * @throws UnknownNodeException Is thrown when the given node doesn't match any renderer.
     */
    public TreeViewRender getRenderer(TreeTableView<XsdNode> node) throws UnknownNodeException {
        Assert.notNull(node, "node cannot be null");

        if (leftTreeRender.getTreeView() == node) {
            return leftTreeRender;
        } else if (rightTreeRender.getTreeView() == node) {
            return rightTreeRender;
        } else {
            throw new UnknownNodeException(node);
        }
    }

    /**
     * Synchronize the scrolling and selection between the 2 tree views.
     */
    public void synchronize() {
        TreeTableView<XsdNode> leftTree = leftTreeRender.getTreeView();
        TreeTableView<XsdNode> rightTree = rightTreeRender.getTreeView();

        this.synchronize(leftTreeRender, rightTreeRender);

        leftTree.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            synchronizeSelection(leftTreeRender, rightTreeRender);
        });
        rightTree.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            synchronizeSelection(rightTreeRender, leftTreeRender);
        });
    }

    /**
     * Refresh the rendered tree views.
     */
    public void refresh() {
        leftTreeRender.refresh();
        rightTreeRender.refresh();
    }

    public void clearAll() {
        leftTreeRender.clear();
        rightTreeRender.clear();
    }

    //endregion

    //region Functions

    /**
     * Synchronize the selection between the tree views.
     *
     * @param selectedTree Set the tree view which is being selected.
     * @param otherTree    Set the unfocused tree view.
     */
    private void synchronizeSelection(TreeViewRender selectedTree, TreeViewRender otherTree) {
        if (!isSelecting && otherTree.isRendering()) {
            isSelecting = true;

            otherTree.getTreeView().getSelectionModel().select(selectedTree.getTreeView().getSelectionModel().getSelectedIndex());

            isSelecting = false;
        }
    }

    //endregion
}

