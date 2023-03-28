package com.compare.xsd.comparison.model.xsd.impl;

import com.compare.xsd.comparison.model.xsd.XsdNode;
import javafx.scene.image.Image;

import java.util.ArrayList;
import java.util.List;

public class XsdEmptyElementNode extends XsdElement {


    public XsdEmptyElementNode(XsdDocument document){
        super(document,null);
    }
    private final List<XsdNode> nodes = new ArrayList<>();

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
        return nodes;
    }

    //endregion

    //region Methods

    /**
     * Add the given node to the inner nodes of this node.
     *
     * @param node Set the node to add.
     */
    public void addNode(XsdEmptyElementNode node) {
        this.nodes.add(node);
    }

    //endregion
}
