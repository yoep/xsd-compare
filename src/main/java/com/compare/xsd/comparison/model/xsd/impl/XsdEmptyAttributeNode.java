package com.compare.xsd.comparison.model.xsd.impl;

import com.compare.xsd.comparison.model.xsd.XsdNode;
import javafx.scene.image.Image;

import java.util.ArrayList;
import java.util.List;

public class XsdEmptyAttributeNode extends XsdAttribute {
    //region Getters & Setters

    @Override
    public Image getIcon() {
        return null;
    }

    @Override
    public String getCardinality() {
        return null;
    }

    @Override
    public List<XsdNode> getNodes() {
        return new ArrayList<>();
    }

    //endregion
}
