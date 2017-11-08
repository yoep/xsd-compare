package com.compare.xsd.models.xsd.impl;

import com.compare.xsd.models.xsd.XsdNode;
import com.sun.org.apache.xerces.internal.impl.xs.XSComplexTypeDecl;
import com.sun.org.apache.xerces.internal.impl.xs.XSElementDecl;
import com.sun.org.apache.xerces.internal.impl.xs.XSModelGroupImpl;
import com.sun.org.apache.xerces.internal.impl.xs.XSParticleDecl;
import com.sun.org.apache.xerces.internal.xs.XSObjectList;
import com.sun.org.apache.xerces.internal.xs.XSTypeDefinition;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.List;

@EqualsAndHashCode
@Getter
public class XsdElement implements XsdNode {
    private final XSElementDecl element;
    private final List<XsdElement> childElements = new ArrayList<>();

    private String name;

    public XsdElement(XSElementDecl element) {
        Assert.notNull(element, "element cannot be null");
        this.element = element;

        init();
    }

    private void init() {
        XSTypeDefinition typeDefinition = element.getTypeDefinition();

        this.name = element.getName();

        if (typeDefinition.getTypeCategory() == XSTypeDefinition.COMPLEX_TYPE) {
            loadComplexType((XSComplexTypeDecl) typeDefinition);
        }
    }

    private void loadComplexType(XSComplexTypeDecl complexType) {
        XSParticleDecl particle = (XSParticleDecl) complexType.getParticle();

        if (particle != null) {
            XSModelGroupImpl group = (XSModelGroupImpl) particle.getTerm();
            XSObjectList children = group.getParticles();

            for (int i = 0; i < children.getLength(); i++) {
                XSParticleDecl child = (XSParticleDecl) children.item(i);

                this.childElements.add(new XsdElement((XSElementDecl) child.getTerm()));
            }
        }
    }
}
