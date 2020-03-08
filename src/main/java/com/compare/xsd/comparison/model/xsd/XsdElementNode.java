package com.compare.xsd.comparison.model.xsd;

public interface XsdElementNode extends XsdNode {
    /**
     * Get the child {@link XsdElementNode} from this {@link XsdElementNode} by the given name.
     * The search of the child xsd element node is case insensitive.
     *
     * @param name The name of the child element to retrieve.
     * @return Returns the element node.
     * @throws NodeNotFoundException Is thrown when the given child element couldn't be found by the given name.
     */
    XsdElementNode getElementByName(String name) throws NodeNotFoundException;

    /**
     * Get the {@link XsdAttributeNode} from this {@link XsdElementNode} by the given name.
     * The search of the attribute node is case insensitive.
     *
     * @param name The name of the attribute to retrieve.
     * @return Returns the attribute node.
     * @throws NodeNotFoundException Is thrown when the given attribute node couldn't be found by the given name.
     */
    XsdAttributeNode findAttributeByName(String name) throws NodeNotFoundException;
}
