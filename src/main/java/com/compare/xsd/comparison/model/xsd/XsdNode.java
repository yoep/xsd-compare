package com.compare.xsd.comparison.model.xsd;

import com.compare.xsd.comparison.model.Change;
import javafx.scene.image.Image;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface XsdNode {

    public enum CompositorType {
        SEQUENCE(1),
        CHOICE(2),
        ALL(3);

        private int compositorType;

        private int value;
        private static Map map;

        private CompositorType(int value) {
            this.value = value;
        }

        static {
            map = new HashMap<>();
            for (CompositorType compositorType : CompositorType.values()) {
                map.put(compositorType.value, compositorType);
            }
        }

        public static CompositorType valueOf(int compositorType) {
            return (CompositorType) map.get(compositorType);
        }

        public int getValue() {
            return value;
        }
    }


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
     * Get the maxinclusive value of the node (optional).
     *
     * @return Returns the maxinclusive value of the node or null.
     */
    Integer getMaxInclusive();

    /**
     * Get the maxexclusive value of the node (optional).
     *
     * @return Returns the maxexclusive value of the node or null.
     */
    Integer getMaxExclusive();

    /**
     * Get the mininclusive value of the node (optional).
     *
     * @return Returns the mininclusive value of the node or null.
     */
    Integer getMinInclusive();

    /**
     * Get the minexclusive value of the node (optional).
     *
     * @return Returns the minexclusive value of the node or null.
     */
    Integer getMinExclusive();

    /**
     * Get the totaldigits value of the node (optional).
     *
     * @return Returns the totaldigits value of the node or null.
     */
    Integer getTotalDigits();

    /**
     * Get the fractionDigits value of the node (optional).
     *
     * @return Returns the fractionDigits value of the node or null.
     */
    Integer getFractionDigits();

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
     * Get the compositor the node.
     * of Xerces interface XSModelGroup
     *     short COMPOSITOR_SEQUENCE = 1;
     *     short COMPOSITOR_CHOICE = 2;
     *     short COMPOSITOR_ALL = 3;
     *
     * @return Returns the compositor of the node.
     */
    short getCompositor();

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
