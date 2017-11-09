package com.compare.xsd.managers;

import com.compare.xsd.model.xsd.XsdNode;
import com.compare.xsd.renderers.TreeViewRender;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.TreeTableView;
import lombok.Data;
import lombok.extern.java.Log;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

@Log
@Data
@Component
public class TreeViewManager {
    private TreeViewRender leftTreeRender;
    private TreeViewRender rightTreeRender;

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
     * Synchronize the scrolling between the 2 tree views.
     */
    public void synchronize() {
        ScrollBar leftScrollBar = (ScrollBar) leftTreeRender.getTreeView().lookup(".scroll-bar");
        ScrollBar rightScrollBar = (ScrollBar) rightTreeRender.getTreeView().lookup(".scroll-bar");

        leftScrollBar.valueProperty().addListener((observable, oldValue, newValue) -> rightScrollBar.setValue(newValue.doubleValue()));
        rightScrollBar.valueProperty().addListener((observable, oldValue, newValue) -> leftScrollBar.setValue(newValue.doubleValue()));
    }
}
