package com.compare.xsd.comparison;

import com.compare.xsd.comparison.model.Change;
import com.compare.xsd.comparison.model.ChangeType;
import com.compare.xsd.comparison.model.xsd.NodeNotFoundException;
import com.compare.xsd.comparison.model.xsd.XsdAttributeNode;
import com.compare.xsd.comparison.model.xsd.XsdNode;
import com.compare.xsd.comparison.model.xsd.impl.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.Assert;

import java.io.File;
import java.util.*;

@Slf4j
@EqualsAndHashCode
@Getter
public class XsdComparer {
    private final XsdDocument oldDocument;
    private final XsdDocument newDocument;

    private int added;
    private int removed;
    private int modified;
    /** The SingleLineChangeTextReport creates only a single text line for every XSD change
     * this harder to read, but assists  to compare the result with other XSD comparison tools */
    private Boolean useSingleLineChangeTextReport = Boolean.FALSE;

    private Map<String, Integer> alreadyComparedAncestor_NewGrammar = new HashMap<>();
    private Map<String, Integer> alreadyComparedAncestor_OldGrammar = new HashMap<>();
    private TextReport textReport;
    /**
     * Initialize a new instance of {@link XsdComparer}.
     *
     * @param oldDocument Set the old document.
     * @param newDocument      Set the new document.
     */
    public XsdComparer(String oldDocument, String newDocument, Boolean useSingleLineChangeTextReport) {
        Assert.notNull(oldDocument, "oldDocument cannot be null");
        Assert.notNull(newDocument, "newDocument cannot be null");
        this.useSingleLineChangeTextReport = useSingleLineChangeTextReport;
        if(useSingleLineChangeTextReport){
            textReport = new SingleLineChangeTextReport();
        }else{
            textReport = new MultiLineChangeTextReport();
        }
        XsdLoader xsdLoader = new XsdLoader(null);
        this.oldDocument = xsdLoader.load(new File(oldDocument));
        log.debug("Finished loading original grammar!");
        this.newDocument = xsdLoader.load(new File(newDocument));
        log.debug("Finished loading new grammar!");
    }


    /**
     * Initialize a new instance of {@link XsdComparer}.
     *
     * @param oldDocument Set the old document.
     * @param newDocument      Set the new document.
     */
    public XsdComparer(XsdDocument oldDocument, XsdDocument newDocument) {
        Assert.notNull(oldDocument, "oldDocument cannot be null");
        Assert.notNull(newDocument, "newDocument cannot be null");
        if(useSingleLineChangeTextReport){
            textReport = new SingleLineChangeTextReport();
        }else{
            textReport = new MultiLineChangeTextReport();
        }
        this.oldDocument = oldDocument;
        this.newDocument = newDocument;
    }

    //region Methods

    /**
     * Compare the old document against the new document.
     *
     * @return Returns true if the comparison was successful, else false.
     */
    public boolean compare() {
        System.out.println(compareAsString());
        return true;
    }

    /**
     * Compare the old document against the new document.
     *
     * @return Returns true if the comparison was successful, else false.
     */
    public String compareAsString() {
        reset();
        try {
            compareAbstractElementNodes(oldDocument, newDocument);
            return textReport.getReport();
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
            return null;
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
     * Compare the old abstract XSD node against the new abstract XSD node.
     *
     * @param oldNode Set the old XSD abstract element node.
     * @param newNode      Set the new XSD abstract element node.
     */
    private void compareAbstractElementNodes(AbstractXsdElementNode oldNode, AbstractXsdElementNode newNode) {
        increaseAncestor_OldGrammar(oldNode);
        increaseAncestor_NewGrammar(newNode);
        Assert.notNull(newNode, "newNode cannot be null");
        assert(oldNode.xpath.equals(newNode.xpath));

        List<XsdElement> oldElementChildren;
        List<XsdElement> newElementChildren;

        if( !alreadyCompared_OldGrammar(oldNode)){
            oldElementChildren = new ArrayList<>(oldNode.getElements()); //take a copy as the actual list might be modified during comparison
        }else{
            oldElementChildren = EMPTY_ARRAY_LIST;
        }

        if(!alreadyCompared_NewGrammar(newNode)){
            newElementChildren = new ArrayList<>(newNode.getElements()); //take a copy as the actual list might be modified during comparison
        }else{
            newElementChildren = EMPTY_ARRAY_LIST;
        }

        //check for removed nodes
        for (XsdElement oldElementChild : oldElementChildren) {
            try {
                XsdElement newChildElement = newNode.findElement(oldElementChild.getName(), "1: old child in new children: ");
                compareXsdElementProperties(oldElementChild, newChildElement);
                compareAbstractElementNodes(oldElementChild, newChildElement);
                assert(!(oldElementChild == null || newChildElement == null || !oldElementChild.xpath.equals(newChildElement.xpath)));
            } catch (NodeNotFoundException ex) {
                assert(newNode.getXPath().equals(oldNode.getXPath()));
                removed++;
                copyElementAsEmptyNode(oldNode.getElements().indexOf(oldElementChild), oldElementChild, newNode);
                Change c = new Change(ChangeType.REMOVED, oldElementChild, null);
                textReport.addChange(c);
                oldElementChild.setChange(c);
            }
        }
        //check for added nodes
        for (XsdElement newElementChild : newElementChildren) {
            try {
                oldNode.findElement(newElementChild.getName(), "new");
            } catch (NodeNotFoundException ex) {
                added++;

                copyElementAsEmptyNode(newNode.getElements().indexOf(newElementChild), newElementChild, oldNode);
                Change c = new Change(ChangeType.ADDED, null, newElementChild);
                textReport.addChange(c);
                newElementChild.setChange(c);
            }
        }
        compareProperties(oldNode, newNode);

        decreaseAncestor_OldGrammar(oldNode);
        decreaseAncestor_NewGrammar(newNode);
    }

    Boolean alreadyCompared_OldGrammar(AbstractXsdElementNode element) {
        return alreadyCompared(element, alreadyComparedAncestor_OldGrammar);
    }

    Boolean alreadyCompared_NewGrammar(AbstractXsdElementNode element){
        return alreadyCompared(element, alreadyComparedAncestor_NewGrammar);
    }

    void increaseAncestor_NewGrammar(AbstractXsdElementNode element){
        increaseAncestor(element, alreadyComparedAncestor_NewGrammar);
    }

    void increaseAncestor_OldGrammar(AbstractXsdElementNode element){
        increaseAncestor(element, alreadyComparedAncestor_OldGrammar);
    }

    void decreaseAncestor_NewGrammar(AbstractXsdElementNode element){
        decreaseAncestor(element, alreadyComparedAncestor_NewGrammar);
    }

    void decreaseAncestor_OldGrammar(AbstractXsdElementNode element){
        decreaseAncestor(element, alreadyComparedAncestor_OldGrammar);
    }

    private static Boolean alreadyCompared(AbstractXsdElementNode element, Map<String, Integer> ancestorMap) {
        // initially the element is a document
        if(element instanceof XsdElement){
            String elementID = ((XsdElement) element).getUniqueId();
            Integer ancestorCount = ancestorMap.get(elementID);
            if(ancestorCount != null && ancestorCount > 2){
                return Boolean.TRUE;
            }else{
                return Boolean.FALSE;
            }
        }
        return Boolean.FALSE;
    }

    private static void increaseAncestor(AbstractXsdElementNode element, Map<String, Integer> ancestorMap) {
        // initially the element is a document
        if(element instanceof XsdElement){
            String elementID = ((XsdElement) element).getUniqueId();
            Integer ancestorCount = ancestorMap.get(elementID);
            if(ancestorCount == null){
                ancestorMap.put(elementID, 1);
            }else{
                ancestorMap.put(elementID, ++ancestorCount);
            }
        }
    }

    private static void decreaseAncestor(AbstractXsdElementNode element, Map<String, Integer> ancestorMap) {
        // initially the element is a document
        if(element instanceof XsdElement){
            String elementID = ((XsdElement) element).getUniqueId();
            Integer ancestorCount = ancestorMap.get(elementID);
            if(ancestorCount == 1){
                ancestorMap.remove(elementID);
            }else{
                ancestorMap.put(elementID, --ancestorCount);
            }
        }
    }

    /**
     * Compare the old XSD element against the new XSD element.
     *
     * @param oldNode Set the old XSD element.
     * @param newNode      Set the new XSD element.
     */
    private void compareXsdElementProperties(XsdElement oldNode, XsdElement newNode) {
        assert(oldNode.xpath.equals(newNode.xpath));

        List<XsdAttribute> oldNodeAttributes = new ArrayList<>(oldNode.getAttributes()); //take a copy as the actual list might be modified during comparison
        List<XsdAttribute> newNodeAttributes = new ArrayList<>(newNode.getAttributes());
        for (XsdAttribute attribute : oldNodeAttributes) {
            try {
                XsdAttributeNode compareAttribute = newNode.findAttributeByName(attribute.getName());
                compareXsdAttributes(attribute, compareAttribute);
            } catch (NodeNotFoundException ex) {
                removed++;
                Change c = new Change(ChangeType.REMOVED, attribute, null);
                textReport.addChange(c);
                attribute.setChange(c);
                copyAttributeAsEmptyNode(oldNode.getAttributes().indexOf(attribute), newNode);
            }
        }

        for (XsdAttribute attribute : newNodeAttributes) {
            try {
                oldNode.findAttributeByName(attribute.getName());
            } catch (NodeNotFoundException ex) {
                added++;
                Change c = new Change(ChangeType.ADDED, null, attribute);
                textReport.addChange(c);
                attribute.setChange(c);
                copyAttributeAsEmptyNode(newNode.getAttributes().indexOf(attribute), oldNode);
            }
        }
    }

    /**
     * Compare the old XSD attribute against the new XSD attribute.
     *
     * @param oldNode Set the old XSD attribute.
     * @param newNode      Set the new XSD attribute.
     */
    private void compareXsdAttributes(XsdAttributeNode oldNode, XsdAttributeNode newNode) {
        compareProperties(oldNode, newNode);
    }

    /**
     * Compare the old XSD node properties against the new XSD node properties.
     *
     * @param oldNode Set the old XSD node.
     * @param newNode      Set the new XSD node.
     */
    private void compareProperties(XsdNode oldNode, XsdNode newNode) {
        Assert.notNull(newNode, "newNode cannot be null");

        if (StringUtils.isNotEmpty(oldNode.getName())) {
            // would be possible to create a change only on demand (but might be used in GUI)
            Change change = new Change(ChangeType.MODIFIED, oldNode, newNode);

            // test change of each facet
            hasNameChanged(oldNode, newNode, change);
            hasTypeNamespaceChanged(oldNode, newNode, change);
            hasTypeChanged(oldNode, newNode, change);
            hasCardinalityChanged(oldNode, newNode, change);
            hasFixedValueChanged(oldNode, newNode, change);
            hasLengthChanged(oldNode, newNode, change);
            hasMinLengthChanged(oldNode, newNode, change);
            hasMaxLengthChanged(oldNode, newNode, change);
            hasPatternChanged(oldNode, newNode, change);
            hasEnumerationChanged(oldNode, newNode, change);
            hasWhitespaceChanged(oldNode, newNode, change);

            if (change.isModified() && !(newNode instanceof XsdDocument)) {
                modified++;
                textReport.addChange(change);
                if(newNode instanceof XsdElement){
                    assert(( ((AbstractXsdNode) newNode).xpath != null && ((AbstractXsdNode) newNode).xpath != null && ((AbstractXsdNode) newNode).xpath.equals(((AbstractXsdNode) oldNode).xpath)));
                    assert(((XsdElement) newNode).getParent() != null);

                }else if(newNode instanceof XsdAttribute) {
                    assert ((((AbstractXsdNode) newNode).getParent().xpath != null && ((AbstractXsdNode) newNode).getParent().xpath != null && ((AbstractXsdNode) newNode).getParent().xpath.equals(((AbstractXsdNode) oldNode).getParent().xpath)));
                }
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
        this.alreadyComparedAncestor_NewGrammar.clear();
        this.alreadyComparedAncestor_OldGrammar.clear();
        this.textReport.reset();;
    }

    private boolean hasNameChanged(XsdNode oldNode, XsdNode newNode, Change modification) {
        boolean change = isPresenceDifferent(oldNode.getName(), newNode.getName()) || isValueDifferent(oldNode.getName(), newNode.getName());
        if(change){
            String item;
            if(oldNode instanceof XsdAttribute){
                item = "attribute";
            }else if(oldNode instanceof XsdElement){
                item = "element";
            }
            if(oldNode instanceof XsdDocument){
                textReport.addDocuments(((XsdDocument) oldNode), ((XsdDocument) newNode));
            }else{
                log.debug("Old Name: " + oldNode.getName());
                log.debug("New Name: " + newNode.getName());
            }
            modification.setNameChanged(true);
        }
        return change;
    }

    private boolean hasTypeNamespaceChanged(XsdNode oldNode, XsdNode newNode, Change modification) {
        boolean change = isPresenceDifferent(oldNode.getTypeNamespace(), newNode.getTypeNamespace()) ||
                isValueDifferent(oldNode.getTypeNamespace(), newNode.getTypeNamespace());
        if(change){
            modification.setNamespaceChanged(true);
        }
        return change;
    }

    private boolean hasTypeChanged(XsdNode oldNode, XsdNode newNode, Change modification) {
        boolean change = isPresenceDifferent(oldNode.getTypeName(), newNode.getTypeName()) || isValueDifferent(oldNode.getTypeName(), newNode.getTypeName());
        if(change){
            modification.setTypeChanged(true);
        }
        return change;
    }

    private boolean hasCardinalityChanged(XsdNode oldNode, XsdNode newNode, Change modification) {
        boolean change = isPresenceDifferent(oldNode.getCardinality(), newNode.getCardinality()) ||
                isValueDifferent(oldNode.getCardinality(), newNode.getCardinality());
        if(change){
            modification.setCardinalityChanged(true);
        }
        return change;
    }

    private boolean hasFixedValueChanged(XsdNode oldNode, XsdNode newNode, Change modification) {
        boolean change = isPresenceDifferent(oldNode.getFixedValue(), newNode.getFixedValue()) ||
                isValueDifferent(oldNode.getFixedValue(), newNode.getFixedValue());
        if(change){
            modification.setFixedValueChanged(true);
        }
        return change;
    }

    private boolean hasPatternChanged(XsdNode oldNode, XsdNode newNode, Change modification) {
        boolean change = isPresenceDifferent(oldNode.getPattern(), newNode.getPattern()) || isValueDifferent(oldNode.getPattern(), newNode.getPattern());
        if(change){
            modification.setPatternChanged(true);
        }
        return change;
    }

    private boolean hasMaxLengthChanged(XsdNode oldNode, XsdNode newNode, Change modification) {
        boolean change = isPresenceDifferent(oldNode.getMaxLength(), newNode.getMaxLength()) ||
                isValueDifferent(oldNode.getMaxLength(), newNode.getMaxLength());
        if(change){
            modification.setMaxLengthChanged(true);
        }
        return change;
    }

    private boolean hasMinLengthChanged(XsdNode oldNode, XsdNode newNode, Change modification) {
        boolean change = isPresenceDifferent(oldNode.getMinLength(), newNode.getMinLength()) ||
                isValueDifferent(oldNode.getMinLength(), newNode.getMinLength());
        if(change){
            modification.setMinLengthChanged(true);
        }
        return change;
    }

    private boolean hasLengthChanged(XsdNode oldNode, XsdNode newNode, Change modification) {
        boolean change = isPresenceDifferent(oldNode.getLength(), newNode.getLength()) || isValueDifferent(oldNode.getLength(), newNode.getLength());
        if(change){
            modification.setLengthChanged(true);
        }
        return change;
    }

    private boolean hasEnumerationChanged(XsdNode oldNode, XsdNode newNode, Change modification) {
        boolean change = false;
        if(oldNode instanceof XsdAttribute){
            change = ((((XsdAttribute) oldNode).getFixedDefaultValue() != null) && ((XsdAttribute) oldNode).getFixedDefaultValue().equals(((XsdAttribute) newNode).getFixedDefaultValue()));
        }else{
            change = (isPresenceDifferent(oldNode.getEnumeration(), newNode.getEnumeration()) ||
                    isValueDifferent(oldNode.getEnumeration(), newNode.getEnumeration()));
        }
        if(change){
            if(oldNode instanceof XsdAttribute){
                change = (((XsdAttribute) oldNode).getFixedDefaultValue()).equals(((XsdAttribute) newNode).getFixedDefaultValue());
                modification.setFixedValueChanged(true);
            }
            modification.setEnumerationChanged(true);
        }
        return change;
    }


    private boolean hasWhitespaceChanged(XsdNode oldNode, XsdNode newNode, Change modification) {
        boolean change = isPresenceDifferent(oldNode.getWhitespace(), newNode.getWhitespace()) ||
                isValueDifferent(oldNode.getWhitespace(), newNode.getWhitespace());
        if(change){
            modification.setWhitespaceChanged(true);
        }
        return change;
    }

    private boolean isPresenceDifferent(Object oldNode, Object newNode) {
        if((oldNode != null && newNode == null) ){
            log.debug(":No longer existent: ");
        }else if(oldNode == null && newNode != null){
            log.debug(":new: ");
        }
        return (oldNode != null && newNode == null) || (oldNode == null && newNode != null);
    }

    private boolean isValueDifferent(Object oldNode, Object newNode) {
        boolean change = (oldNode != null && newNode == null) || (oldNode == null && newNode != null) || (oldNode != null && newNode != null && !oldNode.equals(newNode));
        if(oldNode != null || newNode != null){
            if((oldNode == null) ){
                if(newNode != null){
                    log.debug("Value no longer existent for " + newNode.toString());
                }
            }else if(!oldNode.equals(newNode)){
                if(newNode != null){
                    log.debug("Changed:");
                }else{
                    log.debug("Value is new:");
                }
            }
        }
        return change;
    }

    //endregion
}
