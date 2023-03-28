package com.compare.xsd.comparison.model.xsd.impl;

import com.compare.xsd.comparison.model.xsd.NodeNotFoundException;
import com.compare.xsd.comparison.model.xsd.XsdAttributeNode;
import javafx.scene.image.Image;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.xerces.impl.xs.XSElementDecl;
import org.apache.xerces.impl.xs.XSLoaderImpl;
import org.apache.xerces.xs.XSConstants;
import org.apache.xerces.xs.XSObject;
import org.springframework.util.Assert;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

@EqualsAndHashCode(callSuper = true)
@Data
public class XsdDocument extends AbstractXsdElementNode {
    private final File file;

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

    /** adds all elements within a document to check for potential loops */
    public Map<String, XsdElement> allElements = new HashMap<>();
    //endregion

    //region Getters & Setters

    @Override
    public String getType() {
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
        return "//";
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
        var model = loader.loadURI(file.getAbsolutePath());
        var elements = model.getComponents(XSConstants.ELEMENT_DECLARATION);

        this.name = file.getName();

        for (Object item : elements.values()) {
            if (item instanceof XSObject) {
                XSObject element = (XSObject) item;

                if (element instanceof XSElementDecl) {
                    XsdElement rootElement = XsdElement.newXsdElement((XSElementDecl) item, this);

                    this.elements.add(rootElement);
                }
            }
        }
    }

    //endregion
}
