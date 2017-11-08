package com.compare.xsd.models.xsd.impl;

import com.sun.org.apache.xerces.internal.impl.xs.XSElementDecl;
import com.sun.org.apache.xerces.internal.impl.xs.XSLoaderImpl;
import com.sun.org.apache.xerces.internal.xs.XSConstants;
import com.sun.org.apache.xerces.internal.xs.XSModel;
import com.sun.org.apache.xerces.internal.xs.XSNamedMap;
import com.sun.org.apache.xerces.internal.xs.XSObject;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.springframework.util.Assert;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

@EqualsAndHashCode
@Getter
public class XsdDocument {
    private final File file;
    private final List<XsdElement> elements = new ArrayList<>();

    public XsdDocument(File file) {
        Assert.notNull(file, "file cannot be null");
        this.file = file;

        init();
    }

    /**
     * Initialize the {@link XsdDocument} by loading the given file.
     */
    private void init() {
        XSLoaderImpl loader = new XSLoaderImpl();
        XSModel model = loader.loadURI(file.getAbsolutePath());
        XSNamedMap elements = model.getComponents(XSConstants.ELEMENT_DECLARATION);

        for (int i = 0; i < elements.getLength(); i++) {
            XSObject item = elements.item(i);

            if (item instanceof XSElementDecl) {
                this.elements.add(new XsdElement((XSElementDecl) item));
            }
        }
    }
}
