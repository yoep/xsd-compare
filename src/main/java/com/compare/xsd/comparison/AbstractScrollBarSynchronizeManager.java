package com.compare.xsd.comparison;

import com.compare.xsd.renderers.RenderView;
import javafx.scene.control.ScrollBar;

public abstract class AbstractScrollBarSynchronizeManager {
    private boolean isScrolling;

    /**
     * Synchronize the scrolling between the two given nodes.
     *
     * @param firstNode  Set the first node to sync.
     * @param secondNode Set the second node to sync.
     */
    public void synchronize(RenderView firstNode, RenderView secondNode) {
        ScrollBar firstScrollBar = (ScrollBar) firstNode.getNode().lookup(".scroll-bar");
        ScrollBar secondScrollBar = (ScrollBar) secondNode.getNode().lookup(".scroll-bar");

        firstScrollBar.valueProperty().addListener((observable, oldValue, newValue) -> scroll(secondNode, secondScrollBar, newValue));
        secondScrollBar.valueProperty().addListener((observable, oldValue, newValue) -> scroll(firstNode, firstScrollBar, newValue));
    }

    private void scroll(RenderView nodeToScroll, ScrollBar scrollBarToScroll, Number newValue) {
        if (!isScrolling && nodeToScroll.isRendering()) {
            isScrolling = true;
            scrollBarToScroll.setValue(newValue.doubleValue());
            isScrolling = false;
        }
    }
}
