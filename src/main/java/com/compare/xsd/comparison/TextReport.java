package com.compare.xsd.comparison;


import com.compare.xsd.comparison.model.Change;
import com.compare.xsd.comparison.model.xsd.impl.XsdDocument;

public interface TextReport {

    public static enum implementation {MULTI_LINE_CHANGE, SINGLE_LINE, ONLY_RESTRICTIONS, ONLY_EXTENSIONS}

    public void addDocuments(XsdDocument oldNode, XsdDocument newNode);

    public void addChange(Change c);

    public String getReport();

    public void reset();
}
