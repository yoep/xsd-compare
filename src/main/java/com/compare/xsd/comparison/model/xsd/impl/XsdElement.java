package com.compare.xsd.comparison.model.xsd.impl;

import com.compare.xsd.comparison.model.xsd.NodeNotFoundException;
import com.compare.xsd.comparison.model.xsd.XsdNode;
import com.sun.org.apache.xerces.internal.impl.dv.xs.XSSimpleTypeDecl;
import com.sun.org.apache.xerces.internal.impl.xs.*;
import com.sun.org.apache.xerces.internal.xs.XSElementDeclaration;
import com.sun.org.apache.xerces.internal.xs.XSObjectList;
import com.sun.org.apache.xerces.internal.xs.XSParticle;
import com.sun.org.apache.xerces.internal.xs.XSTypeDefinition;
import javafx.scene.image.Image;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.Assert;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.ArrayList;
import java.util.List;

@Log4j2
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
     * @param parent  Set the parent document.
     */
    public XsdElement(XSElementDecl element, XsdDocument parent) {
        Assert.notNull(element, "element cannot be null");
        this.element = element;
        this.parent = parent;
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
                .filter(e -> e.getName().equalsIgnoreCase(name))
                .findFirst()
                .orElseThrow(() -> new NodeNotFoundException(name));
    }

    /**
     * Insert the given attribute at the given index.
     *
     * @param index     Set the index of the attribute.
     * @param attribute Set the attribute to add.
     */
    public void insertAttributeAt(int index, XsdAttribute attribute) {
        Assert.notNull(attribute, "attribute cannot be null");

        this.attributes.add(index, attribute);
    }

    //endregion

    //region Functions

    private void init() {
        XSTypeDefinition typeDefinition = element.getTypeDefinition();

        this.name = element.getName();
        log.trace("Processing element " + this.name);
        loadNamespace();

        if (typeDefinition.getTypeCategory() == XSTypeDefinition.COMPLEX_TYPE) {
            loadComplexType((XSComplexTypeDecl) typeDefinition);
        } else if (typeDefinition.getTypeCategory() == XSTypeDefinition.SIMPLE_TYPE) {
            loadSimpleType((XSSimpleTypeDecl) typeDefinition);
        } else {
            log.warn("Unknown element type " + typeDefinition.getTypeCategory());
        }
    }

    private void loadComplexType(XSComplexTypeDecl complexType) {
        XSParticleDecl particle = (XSParticleDecl) complexType.getParticle();
        XSObjectList attributes = complexType.getAttributeUses();

        if (particle != null) {
            XSModelGroupImpl group = (XSModelGroupImpl) particle.getTerm();
            processComplexGroup(group);
        } else {
            loadType(complexType);
        }

        if (CollectionUtils.isNotEmpty(attributes)) {
            for (Object attribute : attributes) {
                this.attributes.add(new XsdAttribute((XSAttributeUseImpl) attribute, this));
            }
        }
    }

    private void processComplexGroup(XSModelGroupImpl group) {
        XSObjectList children = group.getParticles();

        for (Object childItem : children) {
            if (childItem instanceof XSParticle) {
                XSParticleDecl child = (XSParticleDecl) childItem;

                if (child.getTerm() instanceof XSElementDeclaration) {
                    this.elements.add(new XsdElement(child, this));
                } else if (child.getTerm() instanceof XSModelGroupImpl) {
                    processComplexGroup((XSModelGroupImpl) child.getTerm());
                }
            }
        }
    }

    private void loadNamespace() {
        this.namespace = element.getNamespace();

        if (StringUtils.isEmpty(this.namespace)) {
            this.namespace = element.getTypeDefinition().getNamespace();

            if (StringUtils.isEmpty(this.namespace) && this.definition != null) {
                this.namespace = this.definition.getNamespace();
            }
        }
    }

    @Override
    protected Element createXml(Document xmlDoc, Element parent) {
        Element xmlElement = super.createXml(xmlDoc, parent);

        for (XsdAttribute attribute : getAttributes()) {
            xmlElement.setAttribute(attribute.getName(), attribute.getXmlValue());
        }

        return xmlElement;
    }

    //endregion
}
