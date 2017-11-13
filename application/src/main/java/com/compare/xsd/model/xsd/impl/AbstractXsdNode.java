package com.compare.xsd.model.xsd.impl;

import com.compare.xsd.model.comparison.ModificationType;
import com.compare.xsd.model.comparison.Modifications;
import com.compare.xsd.model.xsd.XsdNode;
import com.sun.org.apache.xerces.internal.xs.XSTypeDefinition;
import javafx.scene.image.Image;
import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.util.Assert;

/**
 * Abstract implementation of the {@link XsdNode}.
 */
@Data
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class AbstractXsdNode implements XsdNode {
    private static final String SCHEMA_DEFINITION = "http://www.w3.org/2001/XMLSchema";
    private static final String ICON_DIRECTORY = "/icons/";

    protected String name;
    protected String type;
    protected String fixedValue;
    protected String pattern;
    protected Integer minOccurrence;
    protected Integer maxOccurrence;
    protected Integer length;
    protected Integer minLength;
    protected Integer maxLength;

    protected AbstractXsdNode parent;
    protected Modifications modifications;

    //region Constructors

    /**
     * Initialize a new instance of {@link AbstractXsdNode}.
     *
     * @param parent Set the parent of this node.
     */
    protected AbstractXsdNode(AbstractXsdNode parent) {
        this.parent = parent;
    }

    //endregion

    //region Getters & Setters

    @Override
    public String getCardinality() {
        return minOccurrence + ".." + (maxOccurrence != null ? maxOccurrence : "*");
    }

    @Override
    public Image getModificationColor() {
        if (modifications != null) {
            switch (modifications.getType()) {
                case ADDED:
                    return loadResourceIcon("green.png");
                case REMOVED:
                    return loadResourceIcon("red.png");
                case MODIFIED:
                    return loadResourceIcon("orange.png");
                case MOVED:
                    return loadResourceIcon("yellow.png");
                default:
                    return null;
            }
        }

        return null;
    }

    //endregion

    //region Methods

    /**
     * Compare if any of the properties are different.
     *
     * @param compareNode Set the node to compare against.
     */
    public void compareProperties(XsdNode compareNode) {
        Assert.notNull(compareNode, "compareNode cannot be null");

        if (this.name != null) {
            this.modifications = new Modifications(ModificationType.NONE);

            if (hasNameChanged(compareNode)) {
                this.modifications.setNameChanged(true);
            }
            if (hasTypeChanged(compareNode)) {
                this.modifications.setTypeChanged(true);
            }
            if (hasCardinalityChanged(compareNode)) {
                this.modifications.setCardinalityChanged(true);
            }
            if (hasFixedValueChanged(compareNode)) {
                this.modifications.setFixedValueChanged(true);
            }
            if (hasLengthChanged(compareNode)) {
                this.modifications.setLengthChanged(true);
            }
            if (hasMinLengthChanged(compareNode)) {
                this.modifications.setMinLengthChanged(true);
            }
            if (hasMaxLengthChanged(compareNode)) {
                this.modifications.setMaxLengthChanged(true);
            }
            if (hasPatternChanged(compareNode)) {
                this.modifications.setPatternChanged(true);
            }

            this.modifications.verify(compareNode);
        }
    }

    //endregion

    //region Functions

    /**
     * Load the base type of the definition and store the value in {@link #type}.
     *
     * @param typeDefinition Set the type definition of the node.
     */
    protected void loadType(XSTypeDefinition typeDefinition) {
        while (typeDefinition.getBaseType() != null && !typeDefinition.getNamespace().equals(SCHEMA_DEFINITION)) {
            typeDefinition = typeDefinition.getBaseType();
        }

        this.type = typeDefinition.getName();
    }

    /**
     * Load the given resource icon.
     *
     * @param name Set the name of the icon to load.
     * @return Returns the load icon.
     */
    protected Image loadResourceIcon(String name) {
        return new Image(getClass().getResourceAsStream(ICON_DIRECTORY + name));
    }

    private boolean hasNameChanged(XsdNode compareNode) {
        return !this.getName().equals(compareNode.getName());
    }

    private boolean hasTypeChanged(XsdNode compareNode) {
        return this.getType() != null && !this.getType().equals(compareNode.getType());
    }

    private boolean hasCardinalityChanged(XsdNode compareNode) {
        return !this.getCardinality().equals(compareNode.getCardinality());
    }

    private boolean hasFixedValueChanged(XsdNode compareNode) {
        return getFixedValue() != null && !getFixedValue().equals(compareNode.getFixedValue());
    }

    private boolean hasPatternChanged(XsdNode compareNode) {
        return getPattern() != null && !getPattern().equals(compareNode.getPattern());
    }

    private boolean hasMaxLengthChanged(XsdNode compareNode) {
        return getMaxLength() != null && !getMaxLength().equals(compareNode.getMaxLength());
    }

    private boolean hasMinLengthChanged(XsdNode compareNode) {
        return getMinLength() != null && !getMinLength().equals(compareNode.getMinLength());
    }

    private boolean hasLengthChanged(XsdNode compareNode) {
        return getLength() != null && !getLength().equals(compareNode.getLength());
    }

    //endregion
}
