package com.compare.xsd.models.xsd;

import javafx.scene.image.Image;

import java.util.List;

public interface XsdNode {
    /**
     * Get the name of the node.
     *
     * @return Returns the name of the node.
     */
    String getName();

    /**
     * Get the type of the node (optional).
     *
     * @return Returns the type of the node or null.
     */
    String getType();

    /**
     * Get the cardinality of the node (optional).
     *
     * @return Returns the cardinality of the node or null.
     */
    String getCardinality();

    /**
     * Get the icon of the node.
     *
     * @return Returns the image icon of the node.
     */
    Image getIcon();

    /**
     * Get the inner nodes of the node (optional).
     *
     * @return  Returns the inner node or null.
     */
    List<XsdNode> getNodes();
}
