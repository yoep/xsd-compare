package com.compare.xsd.comparison.model.xsd.impl;

import com.compare.xsd.comparison.model.xsd.XsdNode;
import com.sun.org.apache.xerces.internal.impl.xs.XSAttributeUseImpl;
import com.sun.org.apache.xerces.internal.xs.XSAttributeDeclaration;
import javafx.scene.image.Image;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
public class XsdAttribute extends AbstractXsdNode {
    private final XSAttributeUseImpl attribute;

    //region Constructors

    /**
     * Initialize a new instance of {@link XsdAttribute}.
     *
     * @param attribute Set the attribute.
     * @param parent    Set the parent element of this node.
     */
    public XsdAttribute(XSAttributeUseImpl attribute, XsdElement parent) {
        super(parent);
        Assert.notNull(attribute, "attribute cannot be null");
        this.attribute = attribute;
        this.maxOccurrence = 1;

        init();
    }

    /**
     * Initialize a new instance of {@link XsdAttribute}.
     * This constructor should only be used by {@link XsdEmptyAttributeNode}.
     */
    protected XsdAttribute() {
        this.attribute = null;
        this.name = "";
    }

    //endregion

    //region Implementation of XsdNode

    @Override
    public Image getIcon() {
        return loadResourceIcon("attribute.png");
    }

    @Override
    public List<XsdNode> getNodes() {
        return new ArrayList<>();
    }

    @Override
    public String getXPath() {
        String xpath = parent.getXPath();

        return xpath + "[@" + getName() + "]";
    }

    @Override
    public String getXml() {
        return null;
    }

    //endregion

    //region Methods

    @Override
    public String getXmlValue() {
        String xmlValue = super.getXmlValue();

        if (CollectionUtils.isNotEmpty(getEnumeration())) {
            xmlValue = getEnumeration().toString();
        } else if (StringUtils.isNotEmpty(getPattern())) {
            xmlValue = getPattern();
        }

        return xmlValue;
    }

    //endregion

    //region Functions

    private void init() {
        XSAttributeDeclaration declaration = this.attribute.getAttrDeclaration();

        this.name = declaration.getName();
        this.minOccurrence = attribute.getRequired() ? 1 : 0;

        loadNamespace(declaration);
        loadSimpleType(declaration.getTypeDefinition());
    }

    private void loadNamespace(XSAttributeDeclaration declaration) {
        this.namespace = declaration.getNamespace();

        if (StringUtils.isEmpty(this.namespace)) {
            this.namespace = declaration.getTypeDefinition().getNamespace();
        }
    }

    //endregion
}
