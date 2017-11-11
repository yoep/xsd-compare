package com.compare.xsd.model.xsd.impl;

import com.compare.xsd.model.comparison.ModificationType;
import com.compare.xsd.model.comparison.Modifications;
import com.compare.xsd.model.xsd.NodeNotFoundException;
import com.compare.xsd.model.xsd.XsdNode;
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
     * @param parent            Set the parent element of this element.
     */
    public XsdElement(XSParticleDecl elementDefinition, XsdElement parent) {
        super(parent);
        Assert.notNull(elementDefinition, "elementDefinition cannot be null");
        this.element = (XSElementDecl) elementDefinition.getTerm();
        this.definition = elementDefinition;
        this.minOccurrence = elementDefinition.getMinOccurs();
        this.maxOccurrence = elementDefinition.getMaxOccursUnbounded() ? null : elementDefinition.getMaxOccurs();

        init();
    }

    /**
     * Initialize a new {@link XsdElement}.
     * This constructor can only be used {@link XsdEmptyElementNode}.
     */
    protected XsdElement() {
        this.element = null;
        this.definition = null;
        this.name = "";
    }

    //endregion

    //region Getters & Setters

    @Override
    public Image getIcon() {
        return loadResourceIcon("element.png");
    }

    @Override
    public List<XsdNode> getNodes() {
        List<XsdNode> nodes = new ArrayList<>(attributes);

        nodes.addAll(elements);

        return nodes;
    }

    //endregion

    //region Methods

    @Override
    public void compare(AbstractXsdElementNode compareNode) {
        super.compare(compareNode);
        List<XsdAttribute> attributesCopy = new ArrayList<>(attributes); //take a copy as the actual list might be modified during comparison
        XsdElement compareElement = (XsdElement) compareNode;

        for (XsdAttribute attribute : attributesCopy) {
            try {
                XsdAttribute compareAttribute = compareElement.findAttribute(attribute.getName());

                attribute.compare(compareAttribute);
            } catch (NodeNotFoundException ex) {
                attribute.setModifications(new Modifications(ModificationType.Removed));
                copyAttributeAsEmptyNode(attributes.indexOf(attribute), compareElement);
            }
        }
    }

    /**
     * Find the attribute by name.
     *
     * @param name Set the name of the attribute.
     * @return Returns the attribute.
     * @throws NodeNotFoundException Is thrown when the attribute couldn't be found.
     */
    public XsdAttribute findAttribute(String name) throws NodeNotFoundException {
        Assert.hasText(name, "name cannot be empty");

        return attributes.stream()
                .filter(e -> e.getName().equals(name))
                .findFirst()
                .orElseThrow(() -> new NodeNotFoundException(name));
    }

    /**
     * Insert the given attribute at the given index.
     *
     * @param index     Set the index of the attribute.
     * @param attribute Set the attribute to add.
     */
    private void insertAttributeAt(int index, XsdAttribute attribute) {
        Assert.notNull(attribute, "attribute cannot be null");

        this.attributes.add(index, attribute);
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
                        this.elements.add(new XsdElement(child, this));
                    }
                }
            }
        } else {
            loadType(complexType);
        }

        if (CollectionUtils.isNotEmpty(attributes)) {
            for (Object attribute : attributes) {
                this.attributes.add(new XsdAttribute((XSAttributeUseImpl) attribute, this));
            }
        }
    }

    /**
     * Copy the attribute and inner attributes of the given to copy node to the destination element at given index.
     *
     * @param index           Set the index to add the nodes at.
     * @param destinationNode Set the destination of the copied nodes.
     */
    private void copyAttributeAsEmptyNode(int index, XsdElement destinationNode) {
        destinationNode.insertAttributeAt(index, new XsdEmptyAttributeNode());
    }

    //endregion
}
