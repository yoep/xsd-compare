package com.compare.xsd.comparison;


import com.compare.xsd.comparison.model.Change;
import com.compare.xsd.comparison.model.xsd.impl.XsdDocument;

public interface TextReport {

    public void addDocuments(XsdDocument oldNode, XsdDocument newNode);

    public void addChange(Change c);

    public String getReport();

    public void reset();
}
