package com.compare.xsd.model.xsd.impl;

import com.compare.xsd.model.comparison.Modifications;
import com.compare.xsd.model.xsd.XsdNode;
import com.sun.org.apache.xerces.internal.xs.XSTypeDefinition;
import javafx.scene.image.Image;
import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;

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
    protected Integer minOccurrence;
    protected Integer maxOccurrence;

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
                case Added:
                    return loadResourceIcon("green.png");
                case Removed:
                    return loadResourceIcon("red.png");
                case Modified:
                    return loadResourceIcon("orange.png");
                case Moved:
                    return loadResourceIcon("yellow.png");
            }
        }

        return null;
    }

    @Override
    public String getPathLevel() {
        String path = "";
        int index = 0;

        if (parent != null) {
            path = parent.getPathLevel();
        }

        return path += "." + index;
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

    //endregion
}
