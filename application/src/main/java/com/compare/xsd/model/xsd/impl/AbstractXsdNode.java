package com.compare.xsd.model.xsd.impl;

import com.compare.xsd.model.comparison.Modifications;
import com.compare.xsd.model.xsd.XsdNode;
import com.sun.org.apache.xerces.internal.xs.XSFacet;
import com.sun.org.apache.xerces.internal.xs.XSMultiValueFacet;
import com.sun.org.apache.xerces.internal.xs.XSSimpleTypeDefinition;
import com.sun.org.apache.xerces.internal.xs.XSTypeDefinition;
import javafx.scene.image.Image;
import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.java.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Abstract implementation of the {@link XsdNode}.
 */
@Log
@Data
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class AbstractXsdNode implements XsdNode {
    private static final String SCHEMA_DEFINITION = "http://www.w3.org/2001/XMLSchema";
    private static final String ICON_DIRECTORY = "/icons/";

    protected String name;
    protected String type;
    protected String fixedValue;
    protected String pattern;
    protected String whitespace;
    protected Integer minOccurrence;
    protected Integer maxOccurrence;
    protected Integer length;
    protected Integer minLength;
    protected Integer maxLength;
    protected List<String> enumeration = new ArrayList<>();

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

    /**
     * Load the simple type definition for this node.
     *
     * @param simpleType Set the simple type definition to load.
     */
    protected void loadSimpleType(XSSimpleTypeDefinition simpleType) {
        loadType(simpleType);

        for (Object facetObject : simpleType.getFacets()) {
            XSFacet facet = (XSFacet) facetObject;

            switch (facet.getFacetKind()) {
                case XSSimpleTypeDefinition.FACET_LENGTH:
                    this.length = Integer.valueOf(facet.getLexicalFacetValue());
                    break;
                case XSSimpleTypeDefinition.FACET_MINLENGTH:
                    this.minLength = Integer.valueOf(facet.getLexicalFacetValue());
                    break;
                case XSSimpleTypeDefinition.FACET_MAXLENGTH:
                    this.maxLength = Integer.valueOf(facet.getLexicalFacetValue());
                    break;
                case XSSimpleTypeDefinition.FACET_PATTERN:
                    this.pattern = facet.getLexicalFacetValue();
                    break;
                case XSSimpleTypeDefinition.FACET_WHITESPACE:
                    this.whitespace = facet.getLexicalFacetValue();
                    break;
                default:
                    log.warning("Facet type " + facet.getFacetKind() + " is not implemented at the moment");
                    break;
            }
        }

        for (Object facetObject : simpleType.getMultiValueFacets()) {
            XSMultiValueFacet facet = (XSMultiValueFacet) facetObject;

            switch (facet.getFacetKind()) {
                case XSSimpleTypeDefinition.FACET_ENUMERATION:
                    this.enumeration.addAll(facet.getLexicalFacetValues());
                    break;
                case XSSimpleTypeDefinition.FACET_PATTERN:
                    this.pattern = String.join(", ", facet.getLexicalFacetValues());
                    break;
                default:
                    log.warning("Multi facet value type " + facet.getFacetKind() + " is not implemented at the moment");
                    break;
            }
        }
    }

    //endregion
}
