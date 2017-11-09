package com.compare.xsd.model.xsd.impl;

import com.sun.org.apache.xerces.internal.impl.dv.xs.XSSimpleTypeDecl;
import com.sun.org.apache.xerces.internal.impl.xs.*;
import com.sun.org.apache.xerces.internal.xs.XSElementDeclaration;
import com.sun.org.apache.xerces.internal.xs.XSObjectList;
import com.sun.org.apache.xerces.internal.xs.XSParticle;
import com.sun.org.apache.xerces.internal.xs.XSTypeDefinition;
import javafx.scene.image.Image;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.java.Log;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.List;

@Log
@EqualsAndHashCode(callSuper = true)
@Data
public class XsdElement extends AbstractXsdElementNode {
    private final XSElementDecl element;
    private final XSParticleDecl definition;
    private final List<XsdAttribute> attributes = new ArrayList<>();

    //region Constructors

    /**
     * Initialize a new {@link XsdElement}.
     *
     * @param element Set the element to process.
     */
    public XsdElement(XSElementDecl element) {
        Assert.notNull(element, "element cannot be null");
        this.element = element;
        this.definition = null;
        this.minOccurrence = 1;
        this.maxOccurrence = 1;

        init();
    }

    /**
     * Initialize a new {@link XsdElement}.
     *
     * @param elementDefinition Set the definition to process.
     */
    public XsdElement(XSParticleDecl elementDefinition) {
        Assert.notNull(elementDefinition, "elementDefinition cannot be null");
        this.element = (XSElementDecl) elementDefinition.getTerm();
        this.definition = elementDefinition;
        this.minOccurrence = elementDefinition.getMinOccurs();
        this.maxOccurrence = elementDefinition.getMaxOccursUnbounded() ? null : elementDefinition.getMaxOccurs();

        init();
    }

    //endregion

    //region Getters & Setters

    @Override
    public Image getIcon() {
        return loadResourceIcon("element.png");
    }

    //endregion

    //region Functions

    private void init() {
        XSTypeDefinition typeDefinition = element.getTypeDefinition();

        this.name = element.getName();
        log.fine("Processing element " + this.name);

        if (typeDefinition.getTypeCategory() == XSTypeDefinition.COMPLEX_TYPE) {
            loadComplexType((XSComplexTypeDecl) typeDefinition);
        } else if (typeDefinition.getTypeCategory() == XSTypeDefinition.SIMPLE_TYPE) {
            loadSimpleType((XSSimpleTypeDecl) typeDefinition);
        } else {
            log.warning("Unknown element type " + typeDefinition.getTypeCategory());
        }
    }

    private void loadSimpleType(XSSimpleTypeDecl simpleType) {
        loadType(simpleType);
    }

    private void loadComplexType(XSComplexTypeDecl complexType) {
        XSParticleDecl particle = (XSParticleDecl) complexType.getParticle();
        XSObjectList attributes = complexType.getAttributeUses();

        if (particle != null) {
            XSModelGroupImpl group = (XSModelGroupImpl) particle.getTerm();
            XSObjectList children = group.getParticles();

            for (Object childItem : children) {
                if (childItem instanceof XSParticle) {
                    XSParticleDecl child = (XSParticleDecl) childItem;

                    if (child.getTerm() instanceof XSElementDeclaration) {
                        this.elements.add(new XsdElement(child));
                    }
                }
            }
        } else {
            loadType(complexType);
        }

        if (CollectionUtils.isNotEmpty(attributes)) {
            for (Object attribute : attributes) {
                this.attributes.add(new XsdAttribute((XSAttributeUseImpl) attribute));
            }
        }
    }

    //endregion
}
