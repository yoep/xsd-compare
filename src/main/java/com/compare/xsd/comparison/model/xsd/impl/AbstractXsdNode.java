package com.compare.xsd.comparison.model.xsd.impl;

import com.compare.xsd.comparison.model.Change;
import com.compare.xsd.comparison.model.xsd.XsdNode;
import javafx.scene.image.Image;
import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.xerces.xs.XSFacet;
import org.apache.xerces.xs.XSMultiValueFacet;
import org.apache.xerces.xs.XSSimpleTypeDefinition;
import org.apache.xerces.xs.XSTypeDefinition;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Abstract implementation of the {@link XsdNode}.
 */
@Slf4j
@Data
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class AbstractXsdNode implements XsdNode {
    private static final String SCHEMA_DEFINITION = "http://www.w3.org/2001/XMLSchema";
    private static final String ICON_DIRECTORY = "/images/";

    public String xpath;
    protected String name;
    protected String typeNamespace;
    protected String typeName;
    protected XSTypeDefinition typeDefinition;
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
    protected Change change;

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

    //region Implementation of XsdNode

    /** getTypeName() may return null in case of an anonymous type in this case the next typeName of an ancestor is returned
     For example, the attribute @format would return null as its parent typeName:
     <xsd:complexType name="FormattedDateTimeType">
        <xsd:sequence>
            <xsd:element name="DateTimeString">
                <xsd:complexType>
                    <xsd:simpleContent>
                        <xsd:extension base="xsd:string">
                            <xsd:attribute name="format" type="qdt:FormattedDateTimeFormatContentType"/>
                        </xsd:extension>
                    </xsd:simpleContent>
                </xsd:complexType>
            </xsd:element>
        </xsd:sequence>
     </xsd:complexType>
    */
    public String getNextTypeName(){
        AbstractXsdNode node = this;
        String typeName = node.getTypeName();
        while(typeName == null){
            node = node.getParent();
            if(node instanceof XsdDocument || node == null){
                return null;
            }
            typeName = node.getTypeName();
        }
        return typeName;
    }


    @Override
    public String getCardinality() {
        return minOccurrence + ".." + (maxOccurrence != null ? maxOccurrence : "*");
    }

    @Override
    public Image getModificationColor() {
        if (change != null) {
            switch (change.getType()) {
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

    @Override
    public String getXPath() {
        return xpath;
    }

    //endregion

    //region Methods


    /** @return the local name with (optional - if existent) the namespace as prefix within curely brackets: {ns}name
     * as universal name as described by James Clark - http://www.jclark.com/xml/xmlns.htm  */
    public static String getUniversalName(String namespace, String name){
        if(namespace != null && !namespace.isEmpty()){
            return "{" + namespace + "}" + name;
        }else{
            return name;
        }
    }
    /**
     * Get the XML value for this node.
     *
     * @return Returns the XML value or null.
     */
    public String getXmlValue() {
        if (StringUtils.isNotEmpty(getFixedValue())) {
            return getFixedValue();
        } else if (CollectionUtils.isNotEmpty(getEnumeration())) {
            return getEnumeration().get(0);
        }

        return null;
    }

    /**
     * Get the XML comment for this node.
     *
     * @return Returns the XML comment or null.
     */
    public String getXmlComment() {
        String comment = "";

        if (CollectionUtils.isNotEmpty(getEnumeration())) {
            comment += " Possible values: " + getEnumeration();
        }
        if (StringUtils.isNotEmpty(getPattern())) {
            comment += " Pattern: '" + getPattern() + "'";
        }
        if (getLength() != null) {
            comment += " Length: " + getLength();
        }
        if (getMinLength() != null) {
            comment += " Min. length: " + getMinLength();
        }
        if (getMaxLength() != null) {
            comment += " Max. length: " + getMaxLength();
        }

        return StringUtils.isNotEmpty(comment) ? comment.trim() : null;
    }

    //endregion

    //region Functions

    /**
     * Load the base type of the definition and store the value in {@link #typeName}.
     *
     * @param typeDefinition Set the type definition of the node.
     */
    protected void loadType(XSTypeDefinition typeDefinition) {
        /* 2DO instead of taking the final type of inheritance tree, when should create a list of all inherited types
        while (typeDefinition.getBaseType() != null && !isTypeDefinitionDefaultXsdSchemaDefinition(typeDefinition)) {
            typeDefinition = typeDefinition.getBaseType();
        }
        */
        this.typeName = typeDefinition.getName();
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
     * Load facets from the simple type definition for this node.
     *
     * @param simpleType Set the simple type definition to load.
     */
    protected void loadSimpleType(XSSimpleTypeDefinition simpleType) {
        loadType(simpleType);

        for (Object facetObject : simpleType.getFacets()) {
            var facet = (XSFacet) facetObject;

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
                    log.warn("Facet type " + facet.getFacetKind() + " is not implemented at the moment");
                    break;
            }
        }

        for (Object facetObject : simpleType.getMultiValueFacets()) {
            var facet = (XSMultiValueFacet) facetObject;

            switch (facet.getFacetKind()) {
                case XSSimpleTypeDefinition.FACET_ENUMERATION:
                    this.enumeration.addAll(facet.getLexicalFacetValues());
                    break;
                case XSSimpleTypeDefinition.FACET_PATTERN:
                    this.pattern = String.join(", ", facet.getLexicalFacetValues());
                    break;
                default:
                    log.warn("Multi facet value type " + facet.getFacetKind() + " is not implemented at the moment");
                    break;
            }
        }
    }

    private boolean isTypeDefinitionDefaultXsdSchemaDefinition(XSTypeDefinition typeDefinition) {
        return Objects.equals(typeDefinition.getNamespace(), SCHEMA_DEFINITION);
    }

    //endregion
}
