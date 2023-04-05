package com.compare.xsd.comparison.model.xsd;

import com.compare.xsd.comparison.model.Change;
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
     * Get the namespace of the node.
     *
     * @return Returns the namespace of the node.
     */
    String getTypeNamespace();

    /**
     * Get the type of the node (optional).
     *
     * @return Returns the name of the type of the node or null.
     */
    String getTypeName();

    /**
     * Get the cardinality of the node (optional).
     *
     * @return Returns the cardinality of the node or null.
     */
    String getCardinality();

    /**
     * Get the fixed value of the node (optional).
     *
     * @return Returns the fixed value of the node or null.
     */
    String getFixedValue();

    /**
     * Get the pattern of the node (optional).
     *
     * @return Returns the pattern of the node or null.
     */
    String getPattern();

    /**
     * Get the whitespace mode of the node (optional).
     *
     * @return Returns the whitespace mode of the node or null.
     */
    String getWhitespace();

    /**
     * Get the xpath of the node.
     *
     * @return Returns the xpath of the node.
     */
    String getXPath();

    /**
     * Get the XML example of the node.
     *
     * @return Returns the XML example of the node.
     */
    String getXml();

    /**
     * Get the length of the node (optional).
     *
     * @return Returns the length of the node or null.
     */
    Integer getLength();

    /**
     * Get the minimum length of the node (optional).
     *
     * @return Returns the minimum length of the node or null.
     */
    Integer getMinLength();

    /**
     * Get the maximum length of the node (optional).
     *
     * @return Returns the maximum length of the node or null.
     */
    Integer getMaxLength();

    /**
     * Get the icon of the node.
     *
     * @return Returns the image icon of the node.
     */
    Image getIcon();

    /**
     * Get the image to display for the modification of the node.
     *
     * @return Returns the fontColor image of the node.
     */
    Image getModificationColor();

    /**
     * Get the inner nodes of the node (optional).
     *
     * @return Returns the inner node or null.
     */
    List<XsdNode> getNodes();

    /**
     * Get the enumeration values of the node.
     *
     * @return Returns the enumeration values of the node or an empty list.
     */
    List<String> getEnumeration();

    /**
     * Get the modifications of this node.
     *
     * @return Returns the modifications.
     */
    Change getChange();

    /**
     * Set the modifications of this node.
     *
     * @param change Set the modifications.
     */
    void setChange(Change change);
}
