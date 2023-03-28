package com.compare.xsd.comparison;

import com.compare.xsd.comparison.model.ModificationType;
import com.compare.xsd.comparison.model.Modifications;
import com.compare.xsd.comparison.model.xsd.NodeNotFoundException;
import com.compare.xsd.comparison.model.xsd.XsdAttributeNode;
import com.compare.xsd.comparison.model.xsd.XsdNode;
import com.compare.xsd.comparison.model.xsd.impl.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
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
            log.error(ex.getMessage(), ex);
            return false;
        }
    }

    @Override
    public String toString() {
        return "Added " + added + ", removed " + removed + ", modified " + modified;
    }

    //endregion

    //region Functions

    private static final List<XsdElement> EMPTY_ARRAY_LIST = new ArrayList<>();

    /**
     * Compare the original abstract XSD node against the new abstract XSD node.
     *
     * @param originalNode Set the original XSD abstract element node.
     * @param newNode      Set the new XSD abstract element node.
     */
    private void compareAbstractElementNodes(AbstractXsdElementNode originalNode, AbstractXsdElementNode newNode) {
        Assert.notNull(newNode, "newNode cannot be null");
        List<XsdElement> elementsCopy;
        if(!alreadyCompared_OldGrammar(originalNode)){
            elementsCopy = new ArrayList<>(originalNode.getElements()); //take a copy as the actual list might be modified during comparison
        }else{
            elementsCopy = EMPTY_ARRAY_LIST;
        }

        List<XsdElement> compareElementsCopy;
        if(!alreadyCompared_OldGrammar(originalNode)){
            compareElementsCopy = new ArrayList<>(newNode.getElements()); //take a copy as the actual list might be modified during comparison
        }else{
            compareElementsCopy = EMPTY_ARRAY_LIST;
        }

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

    boolean alreadyCompared_NewGrammar(AbstractXsdElementNode element){
        String ns = element.getNamespace();
        String name = element.getName();
        if(ns != null && !ns.isEmpty()){
            name = "{" + ns + "}" + name;
        }
        if(!alreadyComparedElements_NewGrammar.containsKey(name)){
            alreadyComparedElements_NewGrammar.put(name, element);
            return Boolean.FALSE;
        }else{
            return Boolean.TRUE;
        }
    }

    boolean alreadyCompared_OldGrammar(AbstractXsdElementNode element){
        String ns = element.getNamespace();
        String name = element.getName();
        if(ns != null && !ns.isEmpty()){
            name = "{" + ns + "}" + name;
        }
        if(!alreadyComparedElements_OldGrammar.containsKey(name)){
            alreadyComparedElements_OldGrammar.put(name, element);
            return Boolean.FALSE;
        }else{
            return Boolean.TRUE;
        }
    }
    private Map alreadyComparedElements_NewGrammar = new HashMap<String, XsdElement>();
    private Map alreadyComparedElements_OldGrammar = new HashMap<String, XsdElement>();

    /**
     * Compare the original XSD element against the new XSD element.
     *
     * @param originalNode Set the original XSD element.
     * @param newNode      Set the new XSD element.
     */
    private void compareXsdElements(XsdElement originalNode, XsdElement newNode) {
        List<XsdAttribute> originalNodeAttributes = new ArrayList<>(originalNode.getAttributes()); //take a copy as the actual list might be modified during comparison
        List<XsdAttribute> newNodeAttributes = new ArrayList<>(newNode.getAttributes());

        compareAbstractElementNodes(originalNode, newNode);

        for (XsdAttribute attribute : originalNodeAttributes) {
            try {
                if (StringUtils.isNoneEmpty(attribute.getName())) {
                    XsdAttributeNode compareAttribute = newNode.findAttributeByName(attribute.getName());

                    compareXsdAttributes(attribute, compareAttribute);
                }
            } catch (NodeNotFoundException ex) {
                removed++;
                attribute.setModifications(new Modifications(ModificationType.REMOVED));
                copyAttributeAsEmptyNode(originalNode.getAttributes().indexOf(attribute), newNode);
            }
        }

        for (XsdAttribute attribute : newNodeAttributes) {
            try {
                if (StringUtils.isNoneEmpty(attribute.getName())) {
                    originalNode.findAttributeByName(attribute.getName());
                }
            } catch (NodeNotFoundException ex) {
                added++;
                attribute.setModifications(new Modifications(ModificationType.ADDED));
                copyAttributeAsEmptyNode(newNode.getAttributes().indexOf(attribute), originalNode);
            }
        }
    }

    /**
     * Compare the original XSD attribute against the new XSD attribute.
     *
     * @param originalNode Set the original XSD attribute.
     * @param newNode      Set the new XSD attribute.
     */
    private void compareXsdAttributes(XsdAttributeNode originalNode, XsdAttributeNode newNode) {
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
            if (hasNamespaceChanged(originalNode, newNode)) {
                modifications.setNamespaceChanged(true);
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
        XsdEmptyElementNode emptyNode;
        if(toCopyNode instanceof XsdElement){
            emptyNode = new XsdEmptyElementNode(((XsdElement)toCopyNode).getDocument());
            emptyNode.setName(toCopyNode.getName());
        }else{
            log.error("Should not occur: Node to copy should be an element!");
            emptyNode = null;
        }
        /*
        for (XsdNode element : toCopyNode.getNodes()) {
            emptyNode.addNode(deepCopyEmptyElementNodes(element));
        }*/

        return emptyNode;
    }

    private void reset() {
        this.added = 0;
        this.removed = 0;
        this.modified = 0;
        this.alreadyComparedElements_NewGrammar.clear();
        this.alreadyComparedElements_OldGrammar.clear();
    }

    private boolean hasNameChanged(XsdNode originalNode, XsdNode newNode) {
        return isPresenceDifferent(originalNode.getName(), newNode.getName()) || isValueDifferent(originalNode.getName(), newNode.getName());
    }

    private boolean hasNamespaceChanged(XsdNode originalNode, XsdNode newNode) {
        return isPresenceDifferent(originalNode.getNamespace(), newNode.getNamespace()) ||
                isValueDifferent(originalNode.getNamespace(), newNode.getNamespace());
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
