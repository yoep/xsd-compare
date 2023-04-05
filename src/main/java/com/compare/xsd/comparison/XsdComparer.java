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

import java.util.*;

@Slf4j
@EqualsAndHashCode
@Getter
public class XsdComparer {
    private final XsdDocument originalDocument;
    private final XsdDocument newDocument;

    private int added;
    private int removed;
    private int modified;

    /** creating a report with a header line and a set of body lines */
    private Map<String, Set<String>> report = new TreeMap<>();
    private String documentFileCompare;
    private Map<String, Integer> alreadyComparedAncestor_NewGrammar = new HashMap<>();
    private Map<String, Integer> alreadyComparedAncestor_OldGrammar = new HashMap<>();
    // Could have added a class for every report item (make it easier to count, embracing its message), but time is running out...
    private static final String ADD_ATTRIBUTE_START = "Added attribute @";
    private int addedAttributesInTypes = 0;
    private int addedAttributesInXML = 0;
    private static final String ADD_ELEMENT_START = "Added element <";
    private int addedElementsInTypes = 0;
    private int addedElementsInXML = 0;
    private static final String MODIFICATION_ELEMENT_START = "Modifying element:";
    private int modifiedElementsInTypes = 0;
    private int modifiedElementsInXML = 0;
    private static final String MODIFICATION_ATTRIBUTE_START = "Modifying attribute:";
    private int modifiedAttributesInTypes = 0;
    private int modifiedAttributesInXML = 0;
    private static final String REMOVE_ELEMENT_START = "Removed element <";
    private int removedElementsInTypes = 0;
    private int removedElementsInXML = 0;
    private static final String REMOVE_ATTRIBUTE_START = "Removed attribute @";
    private int removedAttributesInTypes = 0;
    private int removedAttributesInXML = 0;

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

    private void updateStatistic(String headerLine, int count){
        String[] parts= headerLine.split(" ");
        String prefix = parts[0];
        String suffix = parts[1];
        switch (prefix) {
            case "Added":
                if(suffix.contains("element")){
                    addedElementsInTypes++;
                    addedElementsInXML+=count;
                }else{
                    addedAttributesInTypes++;
                    addedAttributesInXML+=count;
                }
                break;
            case "Modifying":
                if(suffix.contains("element:")){
                    modifiedElementsInTypes++;
                    modifiedElementsInXML+=count;
                }else{
                    modifiedAttributesInTypes++;
                    modifiedAttributesInXML+=count;
                }
                break;
            case "Removed":
                if(suffix.contains("element")){
                    removedElementsInTypes++;
                    removedElementsInXML+=count;
                }else{
                    removedAttributesInTypes++;
                    removedAttributesInXML+=count;
                }
                break;
            default:
                System.err.println("INVALID COMMAND!");
        }
    }

/*
The function below creates a statistic similar to the one below:

**** STATISTIC ****


ELEMENTS:

	Added elements in XSD:	98
	Added elements in XML:	1828

	Modified elements in XSD:	18
	Modified elements in XML:	175

	Removed elements from XSD:	6
	Removed elements from XML:	45


ATTRIBUTES:

	Added attributes in XSD:	5
	Added attributes in XML:	10

	Modified attributes in XSD:	11
	Modified attributes in XML:	136

	Removed attributes from XSD:	141
	Removed attributes from XML:	8703

 */
    private String getStatistic(){
        StringBuilder statistic = new StringBuilder();
        statistic.append("\n**** STATISTIC ****");
        statistic.append("\n\n\nELEMENTS:");
        statistic.append("\n\n\tAdded elements in XSD:\t").append(addedElementsInTypes);
        statistic.append("\n\tAdded elements in XML:\t").append(addedElementsInXML);
        statistic.append("\n\n\tModified elements in XSD:\t").append(modifiedElementsInTypes);
        statistic.append("\n\tModified elements in XML:\t").append(modifiedElementsInXML);
        statistic.append("\n\n\tRemoved elements from XSD:\t").append(removedElementsInTypes);
        statistic.append("\n\tRemoved elements from XML:\t").append(removedElementsInXML);
        statistic.append("\n\n\nATTRIBUTES:");
        statistic.append("\n\n\tAdded attributes in XSD:\t").append(addedAttributesInTypes);
        statistic.append("\n\tAdded attributes in XML:\t").append(addedAttributesInXML);
        statistic.append("\n\n\tModified attributes in XSD:\t").append(modifiedAttributesInTypes);
        statistic.append("\n\tModified attributes in XML:\t").append(modifiedAttributesInXML);
        statistic.append("\n\n\tRemoved attributes from XSD:\t").append(removedAttributesInTypes);
        statistic.append("\n\tRemoved attributes from XML:\t").append(removedAttributesInXML);
        return statistic.toString();
    }

    /**
     * Compare the original document against the new document.
     *
     * @return Returns true if the comparison was successful, else false.
     */
    public boolean compare() {
        System.out.println(compareAsString());
        return true;
    }

    /**
     * Compare the original document against the new document.
     *
     * @return Returns true if the comparison was successful, else false.
     */
    public String compareAsString() {
        reset();
        try {
            compareAbstractElementNodes(originalDocument, newDocument);
            return getReport();
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
     * Compare the original abstract XSD node against the new abstract XSD node.
     *
     * @param oldNode Set the original XSD abstract element node.
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
                oldElementChild.setModifications(new Modifications(ModificationType.REMOVED));
                copyElementAsEmptyNode(oldNode.getElements().indexOf(oldElementChild), oldElementChild, newNode);
                addReportItem(REMOVE_ELEMENT_START + oldElementChild.getName() + "> {" + oldElementChild.getTypeName() + "}{" + oldElementChild.getCardinality() + "} in type {" + oldElementChild.getParent().getTypeName() + "}",
                        "in " + oldElementChild.getParent().getName() + " at " + oldElementChild.getXPath());
            }
        }
        //check for added nodes
        for (XsdElement newElementChild : newElementChildren) {
            try {
                oldNode.findElement(newElementChild.getName(), "new");
            } catch (NodeNotFoundException ex) {
                added++;
                newElementChild.setModifications(new Modifications(ModificationType.ADDED));
                copyElementAsEmptyNode(newNode.getElements().indexOf(newElementChild), newElementChild, oldNode);
                addReportItem(ADD_ELEMENT_START + newElementChild.getName() + "> {" + newElementChild.getTypeName() + "}{" + newElementChild.getCardinality() + "} in type {" + newElementChild.getParent().getTypeName() + "}",
                        "in " + newElementChild.getParent().getName() + " at " + newElementChild.getXPath());
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
     * Compare the original XSD element against the new XSD element.
     *
     * @param oldNode Set the original XSD element.
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
                addReportItem(REMOVE_ATTRIBUTE_START + attribute.getName() + " {" + attribute.getTypeName() + "}{" + attribute.getCardinality() + "} in type {" + attribute.getParent().getTypeName() + "}",
                        "in " + attribute.getParent().getName() + " at " + attribute.getXPath());
                attribute.setModifications(new Modifications(ModificationType.REMOVED));
                copyAttributeAsEmptyNode(oldNode.getAttributes().indexOf(attribute), newNode);
            }
        }

        for (XsdAttribute attribute : newNodeAttributes) {
            try {
                oldNode.findAttributeByName(attribute.getName());
            } catch (NodeNotFoundException ex) {
                added++;
                attribute.setModifications(new Modifications(ModificationType.ADDED));
                copyAttributeAsEmptyNode(newNode.getAttributes().indexOf(attribute), oldNode);
                addReportItem(ADD_ATTRIBUTE_START + attribute.getName() + " {" + attribute.getTypeName() + "}{" + attribute.getCardinality() + "} in type {" + attribute.getParent().getTypeName() + "}",
                        "in " + attribute.getParent().getName() + " at " + attribute.getXPath());
            }
        }
    }

    /**
     * Compare the original XSD attribute against the new XSD attribute.
     *
     * @param oldNode Set the original XSD attribute.
     * @param newNode      Set the new XSD attribute.
     */
    private void compareXsdAttributes(XsdAttributeNode oldNode, XsdAttributeNode newNode) {
        compareProperties(oldNode, newNode);
    }

    /**
     * Compare the original XSD node properties against the new XSD node properties.
     *
     * @param oldNode Set the original XSD node.
     * @param newNode      Set the new XSD node.
     */
    private void compareProperties(XsdNode oldNode, XsdNode newNode) {
        Assert.notNull(newNode, "newNode cannot be null");

        if (StringUtils.isNotEmpty(oldNode.getName())) {
            // would be possible to create a modification only on demand (but might be used in GUI)
            Modifications modifications = new Modifications(oldNode, newNode);

            // test change of each facet
            hasNameChanged(oldNode, newNode, modifications);
            hasTypeNamespaceChanged(oldNode, newNode, modifications);
            hasTypeChanged(oldNode, newNode, modifications);
            hasCardinalityChanged(oldNode, newNode, modifications);
            hasFixedValueChanged(oldNode, newNode, modifications);
            hasLengthChanged(oldNode, newNode, modifications);
            hasMinLengthChanged(oldNode, newNode, modifications);
            hasMaxLengthChanged(oldNode, newNode, modifications);
            hasPatternChanged(oldNode, newNode, modifications);
            hasEnumerationChanged(oldNode, newNode, modifications);
            hasWhitespaceChanged(oldNode, newNode, modifications);

            if (modifications.isModified() && !(newNode instanceof XsdDocument)) {
                String xPath = "\t\t\t";
                if(newNode instanceof XsdElement){
                    assert(( ((AbstractXsdNode) newNode).xpath != null && ((AbstractXsdNode) newNode).xpath != null && ((AbstractXsdNode) newNode).xpath.equals(((AbstractXsdNode) oldNode).xpath)));
                    assert(((XsdElement) newNode).getParent() != null);
                    xPath += ((AbstractXsdNode) newNode).xpath;
                }else if(newNode instanceof XsdAttribute) {
                    assert(( ((AbstractXsdNode) newNode).getParent().xpath != null && ((AbstractXsdNode) newNode).getParent().xpath != null && ((AbstractXsdNode) newNode).getParent().xpath.equals(((AbstractXsdNode) oldNode).getParent().xpath)));
                    xPath += newNode.getXPath();
                }
                addReportItem(modifications.getHeaderLine(), xPath);
                modified++;
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
        this.removedElementsInTypes = 0;
        this.removedElementsInXML = 0;
        this.removedAttributesInTypes = 0;
        this.removedAttributesInXML = 0;
        this.addedAttributesInTypes = 0;
        this.addedAttributesInXML = 0;
        this.addedElementsInTypes = 0;
        this.addedElementsInXML = 0;
        this.alreadyComparedAncestor_NewGrammar.clear();
        this.alreadyComparedAncestor_OldGrammar.clear();
        this.report.clear();;
    }

    private boolean hasNameChanged(XsdNode oldNode, XsdNode newNode, Modifications modifications) {
        boolean change = isPresenceDifferent(oldNode.getName(), newNode.getName()) || isValueDifferent(oldNode.getName(), newNode.getName());
        if(change){
            String item;
            if(oldNode instanceof XsdAttribute){
                item = "attribute";
            }else if(oldNode instanceof XsdElement){
                item = "element";
            }
            if(oldNode instanceof XsdDocument){
                documentFileCompare =   "**** XSD COMPARISON ****" +
                                        "\n\t old grammar: " + oldNode.getName() +
                                        "\n\t new grammar: " + newNode.getName() + "\n";
            }else{
                log.debug("Old Name: " + oldNode.getName());
                log.debug("New Name: " + newNode.getName());
            }
            modifications.setNameChanged(true);
        }
        return change;
    }

    private boolean hasTypeNamespaceChanged(XsdNode oldNode, XsdNode newNode, Modifications modifications) {
        boolean change = isPresenceDifferent(oldNode.getTypeNamespace(), newNode.getTypeNamespace()) ||
                isValueDifferent(oldNode.getTypeNamespace(), newNode.getTypeNamespace());
        if(change){
            StringBuilder sb = modifications.getStringBuilder();
            sb.append("\n\t\tChanged type namespace from " + oldNode.getTypeNamespace() + " to " + newNode.getTypeNamespace());
            modifications.setNamespaceChanged(true);
        }
        return change;
    }

    private boolean hasTypeChanged(XsdNode oldNode, XsdNode newNode, Modifications modifications) {
        boolean change = isPresenceDifferent(oldNode.getTypeName(), newNode.getTypeName()) || isValueDifferent(oldNode.getTypeName(), newNode.getTypeName());
        if(change){
            StringBuilder sb = modifications.getStringBuilder();
            sb.append("\n\t\tChanged type from " + oldNode.getTypeName() + " to " + newNode.getTypeName());
            modifications.setTypeChanged(true);
        }
        return change;
    }

    private boolean hasCardinalityChanged(XsdNode oldNode, XsdNode newNode, Modifications modifications) {
        boolean change = isPresenceDifferent(oldNode.getCardinality(), newNode.getCardinality()) ||
                isValueDifferent(oldNode.getCardinality(), newNode.getCardinality());
        if(change){
            StringBuilder sb = modifications.getStringBuilder();
            sb.append("\n\t\tChanged cardinality from " + oldNode.getCardinality() + " to " + newNode.getCardinality());
            modifications.setCardinalityChanged(true);
        }
        return change;
    }

    private boolean hasFixedValueChanged(XsdNode oldNode, XsdNode newNode, Modifications modifications) {
        boolean change = isPresenceDifferent(oldNode.getFixedValue(), newNode.getFixedValue()) ||
                isValueDifferent(oldNode.getFixedValue(), newNode.getFixedValue());
        if(change){
            StringBuilder sb = modifications.getStringBuilder();
            sb.append("\n\t\tChanged fixedValue from " + oldNode.getFixedValue() + " to " + newNode.getFixedValue());
            modifications.setFixedValueChanged(true);
        }
        return change;
    }

    private boolean hasPatternChanged(XsdNode oldNode, XsdNode newNode, Modifications modifications) {
        boolean change = isPresenceDifferent(oldNode.getPattern(), newNode.getPattern()) || isValueDifferent(oldNode.getPattern(), newNode.getPattern());
        if(change){
            StringBuilder sb = modifications.getStringBuilder();
            sb.append("\n\t\tChanged pattern from " + oldNode.getPattern() + " to " + newNode.getPattern());
            modifications.setPatternChanged(true);
        }
        return change;
    }

    private boolean hasMaxLengthChanged(XsdNode oldNode, XsdNode newNode, Modifications modifications) {
        boolean change = isPresenceDifferent(oldNode.getMaxLength(), newNode.getMaxLength()) ||
                isValueDifferent(oldNode.getMaxLength(), newNode.getMaxLength());
        if(change){
            StringBuilder sb = modifications.getStringBuilder();
            sb.append("\n\t\tChanged maxLength from " + oldNode.getMaxLength() + " to " + newNode.getMaxLength());
            modifications.setMaxLengthChanged(true);
        }
        return change;
    }

    private boolean hasMinLengthChanged(XsdNode oldNode, XsdNode newNode, Modifications modifications) {
        boolean change = isPresenceDifferent(oldNode.getMinLength(), newNode.getMinLength()) ||
                isValueDifferent(oldNode.getMinLength(), newNode.getMinLength());
        if(change){
            StringBuilder sb = modifications.getStringBuilder();
            sb.append("\n\t\tChanged minLength from " + oldNode.getMinLength() + " to " + newNode.getMinLength());
            modifications.setMinLengthChanged(true);
        }
        return change;
    }

    private boolean hasLengthChanged(XsdNode oldNode, XsdNode newNode, Modifications modifications) {
        boolean change = isPresenceDifferent(oldNode.getLength(), newNode.getLength()) || isValueDifferent(oldNode.getLength(), newNode.getLength());
        if(change){
            StringBuilder sb = modifications.getStringBuilder();
            sb.append("\n\t\tChanged length from " + oldNode.getLength() + " to " + newNode.getLength());
            modifications.setLengthChanged(true);
        }
        return change;
    }

    private boolean hasEnumerationChanged(XsdNode oldNode, XsdNode newNode, Modifications modifications) {
        boolean change = false;
        if(oldNode instanceof XsdAttribute){
            change = (getFixedDefaultValue((XsdAttribute) oldNode) == getFixedDefaultValue((XsdAttribute) newNode)) && (getFixedDefaultValue((XsdAttribute) oldNode) != null);
        }else{
            change = (isPresenceDifferent(oldNode.getEnumeration(), newNode.getEnumeration()) ||
                    isValueDifferent(oldNode.getEnumeration(), newNode.getEnumeration()));
        }
        if(change){
            if(oldNode instanceof XsdAttribute){
                StringBuilder sb = modifications.getStringBuilder();
                sb.append("\n\t\tChanged fixed default from " + getFixedDefaultValue((XsdAttribute) oldNode) + " to " + getFixedDefaultValue((XsdAttribute) newNode));
                change = (getFixedDefaultValue((XsdAttribute) oldNode) == getFixedDefaultValue((XsdAttribute) newNode));
                modifications.setFixedValueChanged(true);
            }else{
                StringBuilder sb = modifications.getStringBuilder();
                sb.append("\n\t\tChanged enumeration from " + oldNode.getEnumeration() + " to " + newNode.getEnumeration());
            }
            modifications.setEnumerationChanged(true);
        }
        return change;
    }


    private boolean hasWhitespaceChanged(XsdNode oldNode, XsdNode newNode, Modifications modifications) {
        boolean change = isPresenceDifferent(oldNode.getWhitespace(), newNode.getWhitespace()) ||
                isValueDifferent(oldNode.getWhitespace(), newNode.getWhitespace());
        if(change){
            StringBuilder sb = modifications.getStringBuilder();
            sb.append("\n\t\tChanged whitespace from " + oldNode.getWhitespace() + " to " + newNode.getWhitespace());
            modifications.setWhitespaceChanged(true);
        }
        return change;
    }


    private String getFixedDefaultValue(XsdAttribute xsdAttr){
        List<String> enumeration = xsdAttr.getEnumeration();
        String fixedDefaultValue = null;
        if(enumeration != null && enumeration.size() == 1){
            fixedDefaultValue = enumeration.get(0);
        }
        if(xsdAttr.getFixedValue() != null && fixedDefaultValue != null){
            Assert.isTrue(fixedDefaultValue.equals(xsdAttr.getFixedValue()), "Simple Node");
        }else if(fixedDefaultValue == null){
            fixedDefaultValue = xsdAttr.getFixedValue();
        }
        return fixedDefaultValue;
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

    /** Report items will be given out to console in the end.
     * There is usually a key line (like an element was removed) and the value lines might differ (the parent element with its XPATH) */
    public void addReportItem(String headerLine, String bodyLine){
        Set<String> bodyLines;
        if(!report.containsKey(headerLine)){
            bodyLines = new TreeSet<>();
        }else{
            bodyLines = report.get(headerLine);
        }
        bodyLines.add(bodyLine);
        report.put(headerLine, bodyLines);
    }

    public String getReport(){
        StringBuilder output = new StringBuilder();
        output.append(documentFileCompare).append("\n").append("\n");
        for(String headerLine : this.report.keySet()){
            output.append(headerLine).append("\n");
            Set<String> bodyLines = report.get(headerLine);
            int count = 0;
            for(String bodyLine : bodyLines){
                output.append("\t").append(bodyLine).append("\n");
                count++;
            }
            updateStatistic(headerLine, count);
            output.append("\n");
        }
        output.append("\n");
        output.append(getStatistic());
        return output.toString();
    }
    //endregion
}
