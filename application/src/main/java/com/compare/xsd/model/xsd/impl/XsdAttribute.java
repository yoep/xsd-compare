package com.compare.xsd.model.xsd.impl;

import com.compare.xsd.model.xsd.XsdNode;
import com.sun.org.apache.xerces.internal.impl.xs.XSAttributeUseImpl;
import com.sun.org.apache.xerces.internal.xs.XSAttributeDeclaration;
import javafx.scene.image.Image;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.util.Assert;

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
     */
    public XsdAttribute(XSAttributeUseImpl attribute) {
        Assert.notNull(attribute, "attribute cannot be null");
        this.attribute = attribute;
        this.maxOccurrence = 1;

        init();
    }

    //endregion

    //region Getters & Setters

    @Override
    public Image getIcon() {
        return loadResourceIcon("attribute.png");
    }

    @Override
    public List<XsdNode> getNodes() {
        return null;
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
