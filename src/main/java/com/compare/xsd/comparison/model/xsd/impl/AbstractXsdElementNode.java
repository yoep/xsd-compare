package com.compare.xsd.comparison.model.xsd.impl;

import com.compare.xsd.comparison.model.xsd.NodeNotFoundException;
import com.compare.xsd.comparison.model.xsd.XsdElementNode;
import com.compare.xsd.comparison.model.xsd.XsdNode;
import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.apache.xerces.dom.DocumentImpl;
import org.springframework.util.Assert;
import org.w3c.dom.Comment;
import org.w3c.dom.DOMConfiguration;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSSerializer;

import java.util.ArrayList;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class AbstractXsdElementNode extends AbstractXsdNode implements XsdElementNode {
    protected final List<XsdElement> elements = new ArrayList<>();
    // required for the central map of all already processed elements
    protected XsdDocument document;

    //region Constructors

    /**
     * Initialize a new instance of {@link AbstractXsdElementNode}.
     *
     * @param parent Set the parent of this node.
     */
    protected AbstractXsdElementNode(AbstractXsdNode parent) {
        super(parent);
    }

    //endregion

    //region Getters & Setters

    @Override
    public List<XsdNode> getNodes() {
        return new ArrayList<>(elements);
    }

    @Override
    public String getXml() {
        var xmlDoc = new DocumentImpl();
        DOMImplementationLS document = (DOMImplementationLS) xmlDoc.getImplementation();
        LSSerializer serializer = document.createLSSerializer();
        DOMConfiguration domConfig = serializer.getDomConfig();

        xmlDoc.setDocumentURI(getDocument().getFile().getAbsolutePath());
        domConfig.setParameter("format-pretty-print", Boolean.TRUE);
        domConfig.setParameter("element-content-whitespace", Boolean.TRUE);

        createXml(xmlDoc, null);

        return serializer.writeToString(xmlDoc);
    }

    //endregion

    //region XsdElementNode

    @Override
    public XsdElementNode getElementByName(String name) throws NodeNotFoundException {
        Assert.notNull(name, "name cannot be null");
        return elements.stream()
                .filter(e -> e.getName().equalsIgnoreCase(name))
                .findFirst()
                .orElseThrow(() -> new NodeNotFoundException(name));
    }

    //endregion

    //region Methods

    /**
     * Find the direct child element by the given name.
     *
     * @param name Set the name of the element to search for.
     * @return Returns the found XSD element.
     * @throws NodeNotFoundException Is thrown when the given element name couldn't be found back.
     */
    public XsdElement findElement(String name) throws NodeNotFoundException {
        Assert.hasText(name, "name cannot be empty");

        return elements.stream()
                .filter(e -> e.getName().equalsIgnoreCase(name))
                .findFirst()
                .orElseThrow(() -> new NodeNotFoundException(name));
    }

    /**
     * Insert the given element at the given index in this node.
     *
     * @param index   Set the index to add the element at.
     * @param element Set the element to add at the given index.
     */
    public void insertElementAt(int index, XsdElement element) {
        Assert.notNull(element, "element cannot be null");

        this.elements.add(index, element);
    }

    //endregion

    //region Functions

    /**
     * Get the XML element representing this XSD node.
     *
     * @param xmlDoc Set the document.
     * @param parent Set the parent of this element.
     * @return Returns the created XML element.
     */
    protected Element createXml(Document xmlDoc, Element parent) {
        Element element = xmlDoc.createElement(getName());
        Comment comment = xmlDoc.createComment(getXmlComment());
        Comment optionalComment = xmlDoc.createComment(minOccurrence == 0 ? "Optional" : null);

        element.setTextContent(getXmlValue());

        if (parent != null) {
            parent.appendChild(element);
            parent.insertBefore(optionalComment, element);
            parent.insertBefore(comment, element);
        } else {
            xmlDoc.appendChild(element);
            xmlDoc.insertBefore(optionalComment, element);
            xmlDoc.insertBefore(comment, element);
        }

        for (AbstractXsdElementNode childElement : getElements()) {
            childElement.createXml(xmlDoc, element);
        }

        return element;
    }

    private XsdDocument getDocument() {
        AbstractXsdNode parent = getParent();

        if (parent != null) {
            while (parent.getParent() != null) {
                parent = parent.getParent();
            }
        }

        return (XsdDocument) parent;
    }

    //endregion
}
