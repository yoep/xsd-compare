package com.compare.xsd.comparison.model.xsd.impl;

import com.compare.xsd.comparison.model.xsd.XsdAttributeNode;
import com.compare.xsd.comparison.model.xsd.XsdNode;
import javafx.scene.image.Image;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.xerces.impl.xs.XSAttributeUseImpl;
import org.apache.xerces.xs.XSAttributeDeclaration;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.List;

import static org.apache.xerces.xs.XSConstants.VC_DEFAULT;
import static org.apache.xerces.xs.XSConstants.VC_FIXED;

@EqualsAndHashCode(callSuper = true)
@Data
public class XsdAttribute extends AbstractXsdNode implements XsdAttributeNode {
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

    public boolean hasDefaultValue(){
        return (attribute.fConstraintType == VC_DEFAULT || attribute.fConstraintType == VC_FIXED);
    }

    public boolean hasFixedDefault(){
        return attribute.fConstraintType == VC_FIXED;
    }

    public String getDefaultValue(){
        if(attribute.fDefault != null){
            return attribute.fDefault.stringValue();
        }else{
            return null;
        }
    }

    /** @return the fixed default value of an attribute
     * NOTE: It might as well be defined via the xs:enumeration of a simple type */
    public String getFixedValue(){
        if(hasFixedDefault()){
            return getDefaultValue();
        }else{
            return null;
        }
    }

    public String getFixedDefaultValue(){
        List<String> enumeration = this.getEnumeration();
        String fixedDefaultValue = null;
        if(enumeration != null && enumeration.size() == 1){
            fixedDefaultValue = enumeration.get(0);
        }
        if(this.getFixedValue() != null && fixedDefaultValue != null){
            Assert.isTrue(fixedDefaultValue.equals(this.getFixedValue()), "Simple Node");
        }else if(fixedDefaultValue == null){
            fixedDefaultValue = this.getFixedValue();
        }
        return fixedDefaultValue;
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
        return parent.xpath + "/@" + getName();
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
        var declaration = this.attribute.getAttrDeclaration();

        this.name = declaration.getName();
        this.minOccurrence = attribute.getRequired() ? 1 : 0;
        this.typeNamespace = loadNamespace(declaration);
        loadSimpleType(declaration.getTypeDefinition());
    }

    private String loadNamespace(XSAttributeDeclaration declaration) {
        String namespace = declaration.getNamespace();

        if (StringUtils.isEmpty( namespace)) {
            namespace = declaration.getTypeDefinition().getNamespace();
        }
        return namespace;
    }

    private XSAttributeUseImpl getXsAttribute(){
            return this.attribute;
    }

    //endregion
}
