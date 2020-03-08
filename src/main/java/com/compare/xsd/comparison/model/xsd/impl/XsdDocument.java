package com.compare.xsd.comparison.model.xsd.impl;

import com.compare.xsd.comparison.model.xsd.NodeNotFoundException;
import com.compare.xsd.comparison.model.xsd.XsdAttributeNode;
import com.compare.xsd.comparison.model.xsd.XsdNode;
import com.sun.org.apache.xerces.internal.impl.xs.XSElementDecl;
import com.sun.org.apache.xerces.internal.impl.xs.XSLoaderImpl;
import com.sun.org.apache.xerces.internal.xs.XSConstants;
import com.sun.org.apache.xerces.internal.xs.XSModel;
import com.sun.org.apache.xerces.internal.xs.XSNamedMap;
import com.sun.org.apache.xerces.internal.xs.XSObject;
import javafx.scene.image.Image;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.util.Assert;

import java.io.File;

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
        XSLoaderImpl loader = new XSLoaderImpl();
        XSModel model = loader.loadURI(file.getAbsolutePath());
        XSNamedMap elements = model.getComponents(XSConstants.ELEMENT_DECLARATION);

        this.name = file.getName();

        for (Object item : elements.values()) {
            if (item instanceof XSObject) {
                XSObject element = (XSObject) item;

                if (element instanceof XSElementDecl) {
                    XsdElement rootElement = new XsdElement((XSElementDecl) item, this);

                    this.elements.add(rootElement);
                }
            }
        }
    }

    //endregion
}
