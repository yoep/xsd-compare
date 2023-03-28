package com.compare.xsd.comparison.model.xsd.impl;

import com.compare.xsd.comparison.model.xsd.NodeNotFoundException;
import com.compare.xsd.comparison.model.xsd.XsdAttributeNode;
import com.compare.xsd.comparison.model.xsd.XsdNode;
import javafx.scene.image.Image;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.xerces.impl.dv.xs.XSSimpleTypeDecl;
import org.apache.xerces.impl.xs.*;
import org.apache.xerces.xs.*;
import org.springframework.util.Assert;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@EqualsAndHashCode(callSuper = true)
@Data
public class XsdElement extends AbstractXsdElementNode {
    private final XSElementDeclaration element;
    private final XSParticle definition;
    private final List<XsdAttribute> attributes = new ArrayList<>();

    //region Constructors

    /**
     * Initialize a new {@link XsdElement}.
     *
     * @param element Set the element to process.
     * @param parent  Set the parent document.
     */
    private XsdElement(XSElementDeclaration element, XsdDocument parent) {
        Assert.notNull(element, "element cannot be null");
        this.element = element;
        // required for centralized map of already processed elements (to prevent endless loops in recursive grammars)
        this.document = parent;
        this.parent = parent;
        this.definition = null;
        this.minOccurrence = 1;
        this.maxOccurrence = 1;
    }

    /** @return the local name with (optional - if existent) the namespace as prefix within curely brackets: {ns}name */
    public static String getNormalizedName(XSObject xsObject){
        String ns = xsObject.getNamespace();
        if(ns != null && !ns.isEmpty()){
            return "{" + ns + "}" + xsObject.getName();
        }else{
            return xsObject.getName();
        }
    }

    public static XsdElement newXsdElement(XSElementDeclaration element, XsdDocument parent) {
        String name = getNormalizedName(element);
        if(parent.allElements.containsKey(name)){
            return parent.allElements.get(name);
        }else{
            XsdElement xsdElement = new XsdElement(element, parent);
            parent.allElements.put(name, xsdElement);
            xsdElement.init();
            return xsdElement;
        }
    }

    public static XsdElement newXsdElement(XSParticle elementDefinition, XsdElement parent) {
        XSElementDeclaration element = (XSElementDecl) elementDefinition.getTerm();
        String name = getNormalizedName(element);
        if(parent.document.allElements.containsKey(name)){
            return parent.document.allElements.get(name);
        }else{
            XsdElement xsdElement = new XsdElement(elementDefinition, parent);
            parent.document.allElements.put(name, xsdElement);
            xsdElement.init();
            return xsdElement;
        }
    }

    /**
     * Initialize a new {@link XsdElement}.
     *
     * @param particle          Only elementDefinition are supported as term to be process.
     * @param parent            Set the parent element of this element.
     */
    private XsdElement(XSParticle particle, XsdElement parent) {
        super(parent);
        Assert.notNull(particle, "elementDefinition cannot be null");
        /** a term could be as well <code>XSModelGroup</code> and <code>XSWildcard</code> */
        XSTerm xsTerm = particle.getTerm();
        if(xsTerm instanceof XSElementDecl){
            this.element = (XSElementDecl) xsTerm;
        }else{
            this.element = null;
            if(xsTerm instanceof XSModelGroup){
                log.warn("XSModelGroup is not supported!");
            }else if(xsTerm instanceof XSWildcard){
                log.warn("XSWildcard is not supported!");
            } else {
                log.error("This should not happen!");
            }
        }
        this.document = parent.document;
        this.definition = particle;
        this.minOccurrence = particle.getMinOccurs();
        this.maxOccurrence = particle.getMaxOccursUnbounded() ? null : particle.getMaxOccurs();
    }

    /**
     * Initialize a new {@link XsdElement}.
     * This constructor can only be used {@link XsdEmptyElementNode}.
     */
    protected XsdElement(XsdDocument document) {
        this.element = null;
        this.definition = null;
        this.name = "";
        this.document = document;
    }

    //endregion

    //region Getters & Setters

    public XsdDocument getDocument(){
        return document;
    }

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

    //region XsdElementNode

    @Override
    public XsdAttributeNode findAttributeByName(String name) throws NodeNotFoundException {
        Assert.notNull(name, "name cannot be null");
        return attributes.stream()
                .filter(e -> e.getName().equalsIgnoreCase(name))
                .findFirst()
                .orElseThrow(() -> new NodeNotFoundException(name));
    }

    //endregion

    //region Methods

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
        var typeDefinition = element.getTypeDefinition();

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
        var particle = (XSParticleDecl) complexType.getParticle();
        var attributes = complexType.getAttributeUses();

        if (particle != null) {
            var group = (XSModelGroupImpl) particle.getTerm();
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
        var children = group.getParticles();

        for (Object childItem : children) {
            if (childItem instanceof XSParticle) {
                XSParticleDecl child = (XSParticleDecl) childItem;

                if (child.getTerm() instanceof XSElementDeclaration) {
                    this.elements.add(newXsdElement(child, this));
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
