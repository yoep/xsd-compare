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
import java.util.Stack;

@Slf4j
@EqualsAndHashCode(callSuper = true)
@Data
public class XsdElement extends AbstractXsdElementNode {
    private XSElementDeclaration elementDecl;
    // particle is not final as an element might have various cardinalites by its surrounding particle
    // 			<xsd:element name="BuyerRequisitionerTradeParty" type="ram:TradePartyType" minOccurs="0" maxOccurs="unbounded"/>
    private XSParticle particle;
    private List<XsdAttribute> attributes = new ArrayList<>();

    //region Constructors

    /**
     * Initialize a new {@link XsdElement}.
     * Just used for the glopal root element declaration
     *
     * @param elementDecl Set the element to process.
     * @param document  Set the containing document.
     */
    private XsdElement(XSElementDeclaration elementDecl, XsdDocument document) {
        Assert.notNull(elementDecl, "element cannot be null");
        this.elementDecl = elementDecl;
        // required for centralized map of already processed elements (to prevent endless loops in recursive grammars)
        this.document = document;
        this.parent = document;
        this.particle = null;
        this.minOccurrence = 1;
        this.maxOccurrence = 1;
        this.typeDefinition = this.elementDecl.getTypeDefinition();
        this.typeName = typeDefinition.getName();
        this.name = elementDecl.getName();
        this.typeNamespace = loadNamespace(this);
        document.xPathStack.push(name);
        this.xpath = createXPath(this.document.xPathStack);
    }

    /**
     * Initialize a new {@link XsdElement}.
     *
     * @param particle          Only elementDefinition are supported as term to be process.
     * @param parent           Set the parent element of this element.
     */
    private XsdElement(XSParticle particle, XsdElement parent, Boolean withinChoice) {
        super(parent);
        Assert.notNull(particle, "elementDefinition cannot be null");
        /** a term could be as well <code>XSModelGroup</code> and <code>XSWildcard</code> */
        XSTerm xsTerm = particle.getTerm();
        if(xsTerm instanceof XSElementDecl){
            this.elementDecl = (XSElementDecl) xsTerm;
        }else{
            this.elementDecl = null;
            if(xsTerm instanceof XSModelGroup){
                log.warn("XSModelGroup is not supported!");
            }else if(xsTerm instanceof XSWildcard){
                log.warn("XSWildcard is not supported!");
            } else {
                log.error("This should not happen!");
            }
        }
        this.particle = particle;
        this.minOccurrence = particle.getMinOccurs();
        this.maxOccurrence = particle.getMaxOccursUnbounded() ? null : particle.getMaxOccurs();
        if(withinChoice && this.minOccurrence == 1){
            this.minOccurrence = 0;
        }
        this.typeDefinition = this.elementDecl.getTypeDefinition();
        this.typeName = typeDefinition.getName();
        this.name = elementDecl.getName();
        this.typeNamespace = loadNamespace(this);
        this.document = parent.getDocument();
        this.document.xPathStack.push(name);
        this.xpath = createXPath(this.document.xPathStack);
    }

    /**
     * Initialize a new {@link XsdElement}.
     * This constructor should only be used {@link XsdEmptyElementNode} or cloning.
     */
    protected XsdElement(XsdDocument document, XSParticle particle) {
        if(particle != null){
            this.elementDecl = (XSElementDeclaration) particle.getTerm();
        }else{
            this.elementDecl = null;
        }
        this.particle = particle;
        this.name = "";
        this.document = document;
    }

    //endregion

    /** Just used for the global root element declaration */
    public static XsdElement newXsdElement(XSElementDeclaration elementDeclaration, XsdDocument document) {
        /** The map is meant to avoid loop triggered by recursive grammars.
         * But there may be similar element names having different content/attributes, therefore the key for this already-read map is the name of the type of an element (either simple or complex).
         * If there is no type the element name is defining a group and is similar unique - but mising test case and certainty, yet ! */

        XsdElement xsdElement = new XsdElement(elementDeclaration, document);
        String elementID = xsdElement.getUniqueId();
        document.allElements.put(elementID, xsdElement);
        raiseAncestorNo(document, elementID);
        xsdElement.loadDescendants();
        document.xPathStack.pop();
        lowerAncestorNo(document, elementID);
        return xsdElement;
    }

    /** We have to use particle as parameter, although we allow only particle.term() being ElementDeclaration as Cardinality is only on particle! */
    public static XsdElement newXsdElement(XSParticle particle, XsdElement parent, Boolean withinChoice) {

        // Cast save as we only allow particle as parameter having particle.term() as ElementDeclaration. Require particle as cardinality is only on particle! */
        XSElementDeclaration elementDeclaration = (XSElementDeclaration) particle.getTerm();
        /** The map is meant to avoid loop triggered by recursive grammars.
         * But there may be similar element names having different content/attributes, therefore the key for this already-read map is the name of the type of an element (either simple or complex).
         * If there is no type the element name is defining a group and is similar unique - but mising test case and certainty, yet ! */
        String elementID = getUniqueId(elementDeclaration, particle);
        if(hasTooManySameAncestors(parent.getDocument(), elementID)){
            return null;
        }else{
            XsdElement xsdElement = new XsdElement(particle, parent, withinChoice);
            // make sure to add the element before initiating the element & creating subcontents
            parent.getDocument().allElements.put(elementID, xsdElement);
            raiseAncestorNo(parent.getDocument(), elementID);
            xsdElement.loadDescendants();
            parent.getDocument().xPathStack.pop();
            lowerAncestorNo(parent.getDocument(), elementID);
            return xsdElement;
        }
    }

    private static String createXPath(Stack<String> ancestorNames){
        StringBuilder xPath = new StringBuilder();
        for(String name : ancestorNames){
            xPath.append("/").append(name);
        }
        return xPath.toString();
    }

    public String getUniqueId(){
        return XsdElement.getUniqueId(elementDecl, minOccurrence, maxOccurrence);
    }

    static private Boolean hasTooManySameAncestors(XsdDocument document, String elementID){
        Integer count = document.ancestorCountByelementIO.get(elementID);
        if(count != null && count > 3){
            return Boolean.TRUE;
        }else{
            return Boolean.FALSE;
        }
    }

    static private void raiseAncestorNo(XsdDocument document, String elementID){
        Integer count = document.ancestorCountByelementIO.get(elementID);
        if(count == null){
            document.ancestorCountByelementIO.put(elementID, 1);
        }else{
            document.ancestorCountByelementIO.put(elementID, ++count);
        }
    }

    static private void lowerAncestorNo(XsdDocument document, String elementID){
        Integer count = document.ancestorCountByelementIO.get(elementID);
        if(count == 1){
            document.ancestorCountByelementIO.remove(elementID);
        }else{
            document.ancestorCountByelementIO.put(elementID, --count);
        }
    }

    private static String getUniqueId(XSElementDeclaration elementDecl, XSParticle particle){
        Integer minOccurrence = particle.getMinOccurs();
        Integer maxOccurrence = particle.getMaxOccursUnbounded() ? null : particle.getMaxOccurs();
        return XsdElement.getUniqueId(elementDecl, minOccurrence, maxOccurrence);
    }

    private static String getUniqueId(XSElementDeclaration elementDecl, Integer minOcc, Integer maxOcc){
        String uniqueId = "";
        if(elementDecl.getTypeDefinition() != null &&  elementDecl.getTypeDefinition().getName() != null){
            uniqueId += elementDecl.getTypeDefinition().getName();
        }
        uniqueId += getUniversalName(elementDecl.getNamespace(), elementDecl.getName());
        String maxOccurString = maxOcc == null ? "n" : maxOcc.toString();
        uniqueId += "{" + minOcc + "," + maxOccurString + "}";
        return uniqueId;
    }

    /** @return the local name with (optional - if existent) the namespace as prefix within curely brackets: {ns}name
     * as universal name as described by James Clark - http://www.jclark.com/xml/xmlns.htm  */
    public static String getUniversalName(String namespace, String name){
        if(namespace != null && !namespace.isEmpty()){
            return "{" + namespace + "}" + name;
        }else{
            return name;
        }
    }
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

    private void loadDescendants() {
        log.trace("Processing element " + this.name);
        if (typeDefinition.getTypeCategory() == XSTypeDefinition.COMPLEX_TYPE) {
            loadComplexType((XSComplexTypeDecl) typeDefinition);
        } else if (typeDefinition.getTypeCategory() == XSTypeDefinition.SIMPLE_TYPE) {
            loadSimpleType((XSSimpleTypeDecl) typeDefinition);
        } else {
            log.warn("Unknown element type " + typeDefinition.getTypeCategory());
        }
    }

    private void loadComplexType(XSComplexTypeDecl complexType) {
        var particleDecl = (XSParticleDecl) complexType.getParticle();
        var attributes = complexType.getAttributeUses();

        if (particleDecl != null) {
            XSModelGroupImpl modelGroup= (XSModelGroupImpl) particleDecl.getTerm();
            /*if(modelGroup.getCompositor() == XSModelGroup.COMPOSITOR_CHOICE){
                processComplexGroup(modelGroup, Boolean.TRUE);
            }else{*/
                processComplexGroup(modelGroup, Boolean.FALSE);
            //}
        } else {
            loadType(complexType);
        }

        if (CollectionUtils.isNotEmpty(attributes)) {
            for (Object attribute : attributes) {
                this.attributes.add(new XsdAttribute((XSAttributeUseImpl) attribute, this));
            }
        }
    }

    private void processComplexGroup(XSModelGroupImpl group, Boolean withinChoice) {
        var particles = group.getParticles();

        for (Object particle : particles) {
            // before JDK 5 generics - particles are always XSParticleDecl
            if (particle instanceof XSParticleDecl) {
                XSParticleDecl particleDecl = (XSParticleDecl) particle;

                if (particleDecl.getTerm() instanceof XSElementDeclaration) {
                    if(((XSElementDeclaration) particleDecl.getTerm()).getName().equals("DateTime") && this.getName().equals("CompleteDateTime")){
                        System.out.println("xxx");
                    }
                    //log.debug("##### Parent element " + this.getName() + " gets child element: " + ((XSElementDeclaration) particleDecl.getTerm()).getName());
                    this.elements.add(newXsdElement(particleDecl, this, withinChoice));
                } else if (particleDecl.getTerm() instanceof XSModelGroupImpl) {
                    XSModelGroupImpl modelGroup= (XSModelGroupImpl) particleDecl.getTerm();
//                    if(modelGroup.getCompositor() == XSModelGroup.COMPOSITOR_CHOICE){
//                        processComplexGroup(modelGroup, Boolean.TRUE);
//                    }else{
                        processComplexGroup(modelGroup, Boolean.FALSE);
//                    }
                }
            }else{
                log.error("Particles should be always be of class XSParticleDecl!");
            }
        }
    }

    private static String loadNamespace(XsdElement element) {
        String namespace = element.elementDecl.getNamespace();

        if (StringUtils.isEmpty(namespace)) {
            namespace = element.elementDecl.getTypeDefinition().getNamespace();

            if (StringUtils.isEmpty(namespace) && element.particle != null) {
                namespace = element.particle.getNamespace();
            }
        }
        return namespace;
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
