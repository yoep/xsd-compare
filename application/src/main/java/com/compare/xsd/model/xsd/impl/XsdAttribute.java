package com.compare.xsd.model.xsd.impl;

import com.compare.xsd.model.xsd.XsdNode;
import com.sun.org.apache.xerces.internal.impl.xs.XSAttributeUseImpl;
import com.sun.org.apache.xerces.internal.xs.XSAttributeDeclaration;
import javafx.scene.image.Image;
import lombok.Data;
import lombok.EqualsAndHashCode;
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

    //region Getters & Setters

    @Override
    public Image getIcon() {
        return loadResourceIcon("attribute.png");
    }

    @Override
    public List<XsdNode> getNodes() {
        return new ArrayList<>();
    }

    //endregion

    //region Methods

    /**
     * Compare this attribute against the given attribute.
     *
     * @param compareAttribute Set the attribute to compare against.
     */
    public void compare(XsdAttribute compareAttribute) {
        Assert.notNull(compareAttribute, "compareAttribute cannot be null");

        //TODO: implement comparison
    }

    //endregion

    //region Functions

    private void init() {
        XSAttributeDeclaration declaration = this.attribute.getAttrDeclaration();

        this.name = declaration.getName();
        this.minOccurrence = attribute.getRequired() ? 1 : 0;

        loadType(declaration.getTypeDefinition());
    }

    //endregion
}
