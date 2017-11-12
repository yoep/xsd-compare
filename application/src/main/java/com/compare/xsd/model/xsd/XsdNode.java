package com.compare.xsd.model.xsd;

import com.compare.xsd.model.comparison.Modifications;
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
     * Get the image to display for the modification of the node.
     *
     * @return Returns the color image of the node.
     */
    Image getModificationColor();

    /**
     * Get the inner nodes of the node (optional).
     *
     * @return  Returns the inner node or null.
     */
    List<XsdNode> getNodes();

    /**
     * Get the modifications of this node.
     *
     * @return Returns the modifications.
     */
    Modifications getModifications();

    /**
     * Set the modifications of this node.
     *
     * @param modifications Set the modifications.
     */
    void setModifications(Modifications modifications);
}
