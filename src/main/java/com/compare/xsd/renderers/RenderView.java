package com.compare.xsd.renderers;

import javafx.scene.Node;

public interface RenderView {
    /**
     * Get is the view is rendering something.
     *
     * @return Returns true if the view is rendering, else false.
     */
    boolean isRendering();

    /**
     * Get the node which is being used for rendering.
     *
     * @return Returns the node.
     */
    Node getNode();
}
