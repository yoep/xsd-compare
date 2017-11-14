package com.compare.xsd.compare;

import com.compare.xsd.model.comparison.ModificationType;
import com.compare.xsd.model.comparison.Modifications;
import com.compare.xsd.model.xsd.NodeNotFoundException;
import com.compare.xsd.model.xsd.XsdNode;
import com.compare.xsd.model.xsd.impl.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.extern.java.Log;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

@Log
@EqualsAndHashCode
@Getter
public class XsdComparer {
    private final XsdDocument originalDocument;
    private final XsdDocument newDocument;

    private int added;
    private int removed;
    private int modified;

    /**
     * Initialize a new instance of {@link XsdComparer}.
     *
     * @param originalDocument Set the original document.
     * @param newDocument      Set the new document.
     */
    public XsdComparer(XsdDocument originalDocument, XsdDocument newDocument) {
        Assert.notNull(originalDocument, "originalDocument cannot be null");
        Assert.notNull(newDocument, "newDocument cannot be null");
        this.originalDocument = originalDocument;
        this.newDocument = newDocument;
    }

    //region Methods

    /**
     * Compare the original document against the new document.
     *
     * @return Returns true if the comparison was successful, else false.
     */
    public boolean compare() {
        reset();

        try {
            compareAbstractElementNodes(originalDocument, newDocument);

            return true;
        } catch (Exception ex) {
            log.log(Level.SEVERE, ex.getMessage(), ex);
            return false;
        }
    }

    @Override
    public String toString() {
        return "Added " + added + ", removed " + removed + ", modified " + modified;
    }

    //endregion

    //region Functions

    /**
     * Compare the original abstract XSD node against the new abstract XSD node.
     *
     * @param originalNode Set the original XSD abstract element node.
     * @param newNode      Set the new XSD abstract element node.
     */
    private void compareAbstractElementNodes(AbstractXsdElementNode originalNode, AbstractXsdElementNode newNode) {
        Assert.notNull(newNode, "newNode cannot be null");
        List<XsdElement> elementsCopy = new ArrayList<>(originalNode.getElements()); //take a copy as the actual list might be modified during comparison
        List<XsdElement> compareElementsCopy = new ArrayList<>(newNode.getElements()); //take a copy as the actual list might be modified during comparison

        //check for removed nodes
        for (XsdElement element : elementsCopy) {
            try {
                if (StringUtils.isNoneEmpty(element.getName())) {
                    XsdElement compareElement = newNode.findElement(element.getName());

                    compareXsdElements(element, compareElement);
                }
            } catch (NodeNotFoundException ex) {
                removed++;
                element.setModifications(new Modifications(ModificationType.REMOVED));
                copyElementAsEmptyNode(originalNode.getElements().indexOf(element), element, newNode);
            }
        }

        //check for added nodes
        for (XsdElement element : compareElementsCopy) {
            try {
                if (StringUtils.isNoneEmpty(element.getName())) {
                    originalNode.findElement(element.getName());
                }
            } catch (NodeNotFoundException ex) {
                added++;
                element.setModifications(new Modifications(ModificationType.ADDED));
                copyElementAsEmptyNode(newNode.getElements().indexOf(element), element, originalNode);
            }
        }

        compareProperties(originalNode, newNode);
    }

    /**
     * Compare the original XSD element against the new XSD element.
     *
     * @param originalNode Set the original XSD element.
     * @param newNode      Set the new XSD element.
     */
    private void compareXsdElements(XsdElement originalNode, XsdElement newNode) {
        List<XsdAttribute> attributesCopy = new ArrayList<>(originalNode.getAttributes()); //take a copy as the actual list might be modified during comparison
        List<XsdAttribute> compareAttributesCopy = new ArrayList<>(newNode.getAttributes());

        compareAbstractElementNodes(originalNode, newNode);

        for (XsdAttribute attribute : attributesCopy) {
            try {
                if (StringUtils.isNoneEmpty(attribute.getName())) {
                    XsdAttribute compareAttribute = newNode.findAttribute(attribute.getName());

                    compareXsdAttributes(attribute, compareAttribute);
                }
            } catch (NodeNotFoundException ex) {
                removed++;
                attribute.setModifications(new Modifications(ModificationType.REMOVED));
                copyAttributeAsEmptyNode(originalNode.getAttributes().indexOf(attribute), newNode);
            }
        }

        for (XsdAttribute attribute : compareAttributesCopy) {
            try {
                if (StringUtils.isNoneEmpty(attribute.getName())) {
                    newNode.findAttribute(attribute.getName());
                }
            } catch (NodeNotFoundException ex) {
                added++;
                attribute.setModifications(new Modifications(ModificationType.ADDED));
                copyAttributeAsEmptyNode(originalNode.getAttributes().indexOf(attribute), originalNode);
            }
        }
    }

    /**
     * Compare the original XSD attribute against the new XSD attribute.
     *
     * @param originalNode Set the original XSD attribute.
     * @param newNode      Set the new XSD attribute.
     */
    private void compareXsdAttributes(XsdAttribute originalNode, XsdAttribute newNode) {
        compareProperties(originalNode, newNode);
    }

    /**
     * Compare the original XSD node properties against the new XSD node properties.
     *
     * @param originalNode Set the original XSD node.
     * @param newNode      Set the new XSD node.
     */
    private void compareProperties(XsdNode originalNode, XsdNode newNode) {
        Assert.notNull(newNode, "newNode cannot be null");

        if (StringUtils.isNotEmpty(originalNode.getName())) {
            Modifications modifications = new Modifications();

            if (hasNameChanged(originalNode, newNode)) {
                modifications.setNameChanged(true);
            }
            if (hasTypeChanged(originalNode, newNode)) {
                modifications.setTypeChanged(true);
            }
            if (hasCardinalityChanged(originalNode, newNode)) {
                modifications.setCardinalityChanged(true);
            }
            if (hasFixedValueChanged(originalNode, newNode)) {
                modifications.setFixedValueChanged(true);
            }
            if (hasLengthChanged(originalNode, newNode)) {
                modifications.setLengthChanged(true);
            }
            if (hasMinLengthChanged(originalNode, newNode)) {
                modifications.setMinLengthChanged(true);
            }
            if (hasMaxLengthChanged(originalNode, newNode)) {
                modifications.setMaxLengthChanged(true);
            }
            if (hasPatternChanged(originalNode, newNode)) {
                modifications.setPatternChanged(true);
            }
            if (hasEnumerationChanged(originalNode, newNode)) {
                modifications.setEnumerationChanged(true);
            }
            if (hasWhitespaceChanged(originalNode, newNode)) {
                modifications.setWhitespaceChanged(true);
            }

            if (modifications.isModified()) {
                modified++;
                modifications.setType(ModificationType.MODIFIED);
                originalNode.setModifications(modifications);
                newNode.setModifications(modifications);
            }
        }
    }

    /**
     * Copy the attribute and inner attributes of the given to copy node to the destination element at given index.
     *
     * @param index           Set the index to add the nodes at.
     * @param destinationNode Set the destination of the copied nodes.
     */
    private void copyAttributeAsEmptyNode(int index, XsdElement destinationNode) {
        destinationNode.insertAttributeAt(index, new XsdEmptyAttributeNode());
    }

    /**
     * Copy the element and inner elements of the given to copy node to the destination element at given index.
     *
     * @param index           Set the index to add the nodes at.
     * @param toCopyNode      Set the node to deep copy.
     * @param destinationNode Set the destination of the copied nodes.
     */
    private void copyElementAsEmptyNode(int index, AbstractXsdElementNode toCopyNode, AbstractXsdElementNode destinationNode) {
        destinationNode.insertElementAt(index, deepCopyEmptyElementNodes(toCopyNode));
    }

    private XsdEmptyElementNode deepCopyEmptyElementNodes(XsdNode toCopyNode) {
        XsdEmptyElementNode emptyNode = new XsdEmptyElementNode();

        for (XsdNode element : toCopyNode.getNodes()) {
            emptyNode.addNode(deepCopyEmptyElementNodes(element));
        }

        return emptyNode;
    }

    private void reset() {
        this.added = 0;
        this.removed = 0;
        this.modified = 0;
    }

    private boolean hasNameChanged(XsdNode originalNode, XsdNode newNode) {
        return isPresenceDifferent(originalNode.getName(), newNode.getName()) || isValueDifferent(originalNode.getName(), newNode.getName());
    }

    private boolean hasTypeChanged(XsdNode originalNode, XsdNode newNode) {
        return isPresenceDifferent(originalNode.getType(), newNode.getType()) || isValueDifferent(originalNode.getType(), newNode.getType());
    }

    private boolean hasCardinalityChanged(XsdNode originalNode, XsdNode newNode) {
        return isPresenceDifferent(originalNode.getCardinality(), newNode.getCardinality()) ||
                isValueDifferent(originalNode.getCardinality(), newNode.getCardinality());
    }

    private boolean hasFixedValueChanged(XsdNode originalNode, XsdNode newNode) {
        return isPresenceDifferent(originalNode.getFixedValue(), newNode.getFixedValue()) ||
                isValueDifferent(originalNode.getFixedValue(), newNode.getFixedValue());
    }

    private boolean hasPatternChanged(XsdNode originalNode, XsdNode newNode) {
        return isPresenceDifferent(originalNode.getPattern(), newNode.getPattern()) || isValueDifferent(originalNode.getPattern(), newNode.getPattern());
    }

    private boolean hasMaxLengthChanged(XsdNode originalNode, XsdNode newNode) {
        return isPresenceDifferent(originalNode.getMaxLength(), newNode.getMaxLength()) ||
                isValueDifferent(originalNode.getMaxLength(), newNode.getMaxLength());
    }

    private boolean hasMinLengthChanged(XsdNode originalNode, XsdNode newNode) {
        return isPresenceDifferent(originalNode.getMinLength(), newNode.getMinLength()) ||
                isValueDifferent(originalNode.getMinLength(), newNode.getMinLength());
    }

    private boolean hasLengthChanged(XsdNode originalNode, XsdNode newNode) {
        return isPresenceDifferent(originalNode.getLength(), newNode.getLength()) || isValueDifferent(originalNode.getLength(), newNode.getLength());
    }

    private boolean hasEnumerationChanged(XsdNode originalNode, XsdNode newNode) {
        return isPresenceDifferent(originalNode.getEnumeration(), newNode.getEnumeration()) ||
                isValueDifferent(originalNode.getEnumeration(), newNode.getEnumeration());
    }

    private boolean hasWhitespaceChanged(XsdNode originalNode, XsdNode newNode) {
        return isPresenceDifferent(originalNode.getWhitespace(), newNode.getWhitespace()) ||
                isValueDifferent(originalNode.getWhitespace(), newNode.getWhitespace());
    }

    private boolean isPresenceDifferent(Object originalNode, Object newNode) {
        return (originalNode != null && newNode == null) || (originalNode == null && newNode != null);
    }

    private boolean isValueDifferent(Object originalNode, Object newNode) {
        return originalNode != null && !originalNode.equals(newNode);
    }

    //endregion
}
