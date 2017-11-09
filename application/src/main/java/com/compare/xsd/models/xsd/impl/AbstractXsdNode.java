package com.compare.xsd.models.xsd.impl;

import com.compare.xsd.models.xsd.XsdNode;
import com.sun.org.apache.xerces.internal.xs.XSTypeDefinition;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

/**
 * Abstract implementation of the {@link XsdNode}.
 */
@EqualsAndHashCode
@ToString
@Getter
public abstract class AbstractXsdNode implements XsdNode {
    private static final String SCHEMA_DEFINITION = "http://www.w3.org/2001/XMLSchema";

    protected String name;
    protected String type;
    protected Integer minOccurrence;
    protected Integer maxOccurrence;

    //region Getters & Setters

    @Override
    public String getCardinality() {
        return minOccurrence + ".." + (maxOccurrence != null ? maxOccurrence : "*");
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

    //endregion
}
