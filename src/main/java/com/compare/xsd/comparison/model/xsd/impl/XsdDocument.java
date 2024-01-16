package com.compare.xsd.comparison.model.xsd.impl;

import com.compare.xsd.comparison.model.xsd.NodeNotFoundException;
import com.compare.xsd.comparison.model.xsd.XsdAttributeNode;
import javafx.scene.image.Image;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.xerces.impl.xs.XSElementDecl;
import org.apache.xerces.impl.xs.XSLoaderImpl;
import org.apache.xerces.impl.xs.XSModelImpl;
import org.apache.xerces.xs.*;
import org.springframework.util.Assert;

import javax.xml.parsers.DocumentBuilder;
import java.io.File;
import java.util.*;

@EqualsAndHashCode(callSuper = true)
@Data
public class XsdDocument extends AbstractXsdElementNode {
    private final File file;
    public Stack<String> xPathStack = new Stack<>();
    public Map<String, Integer> ancestorCountByelementIO = new HashMap();
    private DocumentBuilder builder;
    public int duplicatedAnchestorNoAllowed = 2;
    private StringBuilder sb;
    private static final String XPATH_ROOT = "/";
    //region Constructors

    /**
     * Initialize a new instance of {@link XsdDocument}.
     *
     * @param file Set the XSD file to load.
     */
    public XsdDocument(File file) {
        Assert.notNull(file, "file cannot be null");
        this.file = file;
        init();
    }

    public StringBuilder getStringBuilder() {
        sb.delete(0, sb.length());
        return sb;
    }

    /**
     * Initialize a new instance of {@link XsdDocument}.
     *
     * @param file Set the XSD file to load.
     * @param duplicatedAnchestorNoAllowed Set the recursion depth by limiting same anchestors
     */
    public XsdDocument(File file, int duplicatedAnchestorNoAllowed) {
        Assert.notNull(file, "file cannot be null");
        this.file = file;
        this.duplicatedAnchestorNoAllowed = duplicatedAnchestorNoAllowed;
        init();
    }

    /** adds all elements within a document to check for potential loops */
    public Map<String, XsdElement> allElements = new HashMap<>();
    //endregion

    //region Getters & Setters

    @Override
    public String getTypeName() {
        return null;
    }

    @Override
    public String getCardinality() {
        return null;
    }

    @Override
    public Image getIcon() {
        return loadResourceIcon("file.png");
    }

    @Override
    public Image getModificationColor() {
        return null;
    }

    @Override
    public String getXPath() {
        return XPATH_ROOT;
    }


    //endregion

    //region XsdElementNode

    @Override
    public XsdAttributeNode findAttributeByName(String name) throws NodeNotFoundException {
        throw new UnsupportedOperationException("getAttributeByName is not supported for " + this.getClass().getSimpleName());
    }

    //endregion

    //region Functions

    /**
     * Initialize the {@link XsdDocument} by loading the given file.
     */
    private void init() {
        var loader = new XSLoaderImpl();
        // XSD is now parsed by Xerces the XSModelImpl holds all information
        XSModelImpl model = (XSModelImpl) loader.loadURI(file.getAbsolutePath());
        // start to bootstrap our model by accessing the map of all root elements
        XSNamedMap elements = model.getComponents(XSConstants.ELEMENT_DECLARATION);

        this.sb = new StringBuilder();
        this.name = file.getName();

        // check every root element
        for (Object item : elements.values()) {
            if (item instanceof XSObject) {
                XSObject element = (XSObject) item;
                if (element instanceof XSElementDecl) {
                    XsdElement rootElement = XsdElement.newXsdElement((XSElementDecl) item, this);
                    this.elements.add(rootElement);
                }
            }else{
                new AssertionError("The member of Xerces 'Element Declaration map should be all of type XSObject!");
            }
        }
    }

    //endregion
}
