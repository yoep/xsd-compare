package com.compare.xsd.comparison;

import com.compare.xsd.comparison.model.Change;
import com.compare.xsd.comparison.model.ChangeType;
import com.compare.xsd.comparison.model.xsd.XsdNode;
import com.compare.xsd.comparison.model.xsd.impl.XsdAttribute;
import com.compare.xsd.comparison.model.xsd.impl.XsdDocument;
import lombok.Data;

import java.util.*;

@Data
public class MultiLineChangeTextReport implements TextReport {
    private String reportHeader = "";
    private String reportFooter = null;

    /** creating a report with a header line and a set of body lines */
    private Map<String, Set<String>> report = new TreeMap<>();
    private List<Change> changes = new ArrayList<>();

    private int addedAttributesInTypes = 0;
    private int addedAttributesInXML = 0;
    private int addedElementsInTypes = 0;
    private int addedElementsInXML = 0;
    private int modifiedElementsInTypes = 0;
    private int modifiedElementsInXML = 0;
    private int modifiedAttributesInTypes = 0;
    private int modifiedAttributesInXML = 0;
    private int removedElementsInTypes = 0;
    private int removedElementsInXML = 0;
    private int removedAttributesInTypes = 0;
    private int removedAttributesInXML = 0;

    public void addDocuments(XsdDocument oldNode, XsdDocument newNode){
        reportHeader =   "**** XSD COMPARISON ****" +
                "\n\t old grammar: " + oldNode.getName() +
                "\n\t new grammar: " + newNode.getName() + "\n\n\n";
    }

    private String getReportHeader(){
        if(reportHeader == null){
            reportHeader = "";
        }
        return reportHeader;
    }
    private void updateStatistic(Change change, boolean newHeaderLine){

        switch (change.type) {
            case ADDED:
                if(change.isElement){
                    if(newHeaderLine)
                        addedElementsInTypes++;
                    addedElementsInXML++;
                }else{
                    if(newHeaderLine)
                        addedAttributesInTypes++;
                    addedAttributesInXML++;
                }
                break;
            case MODIFIED:
                if(change.isElement){
                    if(newHeaderLine)
                        modifiedElementsInTypes++;
                    modifiedElementsInXML++;
                }else{
                    if(newHeaderLine)
                        modifiedAttributesInTypes++;
                    modifiedAttributesInXML++;
                }
                break;
            case REMOVED:
                if(change.isElement){
                    if(newHeaderLine)
                        removedElementsInTypes++;
                    removedElementsInXML++;
                }else{
                    if(newHeaderLine)
                        removedAttributesInTypes++;
                    removedAttributesInXML++;
                }
                break;
            default:
                System.err.println("INVALID CHANGE!");
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
    private String getReportFooter(){
        if(reportFooter == null) {
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
            reportFooter = statistic.toString();
        }
        return reportFooter;

    }

    private void createChangeMessage(Change c){
        if (c.type == ChangeType.ADDED) {
            if (c.isElement) {
                c.setReportHeader("Added element <" + c.newNode.getName() + "> {" + c.newNode.getTypeName() + "}{" + c.newNode.getCardinality() + "} in type {" + c.newNode.getParent().getTypeName() + "}");
                c.setReportBody("in " + c.newNode.getParent().getName() + " at " + c.newNode.getXPath());
            } else {
                c.setReportHeader("Added attribute @" + c.newNode.getName() + " {" + c.newNode.getTypeName() + "}{" + c.newNode.getCardinality() + "} in type {" + c.newNode.getParent().getTypeName() + "}");
                c.setReportBody("in " + c.newNode.getParent().getName() + " at " + c.newNode.getXPath());
            }
        } else if (c.type == ChangeType.MODIFIED) {
            c.setReportHeader("Modifying " + (c.isElement ? "element: " : "attribute: ") +
                    "\n\told: " + (c.isElement ? "<" + c.oldNode.getName() + ">" : "@" + c.oldNode.getName()) + "{" + c.oldNode.getNextTypeName() + "}{" + c.oldNode.getCardinality() + "} in type {" + c.oldNode.getParent().getNextTypeName() + "}" +
                    "\n\tnew: " + (c.isElement ? "<" + c.newNode.getName() + ">" : "@" + c.newNode.getName()) + "{" + c.newNode.getNextTypeName() + "}{" + c.newNode.getCardinality() + "} in type {" + c.newNode.getParent().getNextTypeName() + "}");
            c.setReportBody("\t\t\tat " + c.newNode.getXPath());
        /*
            "Changed type namespace from " + oldNode.getTypeNamespace() + " to " + newNode.getTypeNamespace()
            "Changed type from " + oldNode.getTypeName() + " to " + newNode.getTypeName()
            "Changed cardinality from " + oldNode.getCardinality() + " to " + newNode.getCardinality()
            "Changed fixedValue from " + oldNode.getFixedValue() + " to " + newNode.getFixedValue()
            "Changed pattern from " + oldNode.getPattern() + " to " + newNode.getPattern()
            "Changed maxLength from " + oldNode.getMaxLength() + " to " + newNode.getMaxLength()
            "Changed minLength from " + oldNode.getMinLength() + " to " + newNode.getMinLength()
            "Changed length from " + oldNode.getLength() + " to " + newNode.getLength()
            "Changed fixed default from " + getFixedDefaultValue((XsdAttribute) oldNode) + " to " + getFixedDefaultValue((XsdAttribute) newNode)
            "Changed enumeration from " + oldNode.getEnumeration() + " to " + newNode.getEnumeration()
            "Changed whitespace from " + oldNode.getWhitespace() + " to " + newNode.getWhitespace()
        */
            if (c.isNamespaceChanged()) {
                c.setReportHeader(c.getReportHeader() + "\n\t\tChanged type namespace from " + c.oldNode.getTypeNamespace() + " to " + c.newNode.getTypeNamespace());
            }
            if (c.isTypeChanged()) {
                c.setReportHeader(c.getReportHeader() + ("\n\t\tChanged type from " + c.oldNode.getTypeName() + " to " + c.newNode.getTypeName()));
            }
            if (c.isCardinalityChanged()) {
                c.setReportHeader(c.getReportHeader() + ("\n\t\tChanged cardinality from " + c.oldNode.getCardinality() + " to " + c.newNode.getCardinality()));
            }
            if (c.isFixedDefaultChanged()) {
                c.setReportHeader(c.getReportHeader() + ("\n\t\tChanged fixed default from " + ((XsdAttribute) c.oldNode).getFixedDefaultValue() + " to " + ((XsdAttribute) c.newNode).getFixedDefaultValue()));
            }
            if (c.isPatternChanged()) {
                c.setReportHeader(c.getReportHeader() + ("\n\t\tChanged pattern from " + c.oldNode.getPattern() + " to " + c.newNode.getPattern()));
            }
            if (c.isMaxLengthChanged()) {
                c.setReportHeader(c.getReportHeader() + ("\n\t\tChanged maxLength from " + c.oldNode.getMaxLength() + " to " + c.newNode.getMaxLength()));
            }
            if (c.isMinLengthChanged()) {
                c.setReportHeader(c.getReportHeader() + ("\n\t\tChanged minLength from " + c.oldNode.getMinLength() + " to " + c.newNode.getMinLength()));
            }
            if (c.isLengthChanged()) {
                c.setReportHeader(c.getReportHeader() + ("\n\t\tChanged length from " + c.oldNode.getLength() + " to " + c.newNode.getLength()));
            }
            if (c.isMaxInclusiveChanged()) {
                c.setReportHeader(c.getReportHeader() + ("\n\t\tChanged maxLengthInclusive from " + c.oldNode.getMaxInclusive() + " to " + c.newNode.getMaxInclusive()));
            }
            if (c.isMaxExclusiveChanged()) {
                c.setReportHeader(c.getReportHeader() + ("\n\t\tChanged maxLengthExclusive from " + c.oldNode.getMaxExclusive() + " to " + c.newNode.getMaxExclusive()));
            }
            if (c.isMinInclusiveChanged()) {
                c.setReportHeader(c.getReportHeader() + ("\n\t\tChanged minLengthInclusive from " + c.oldNode.getMinInclusive() + " to " + c.newNode.getMinInclusive()));
            }
            if (c.isMinExclusiveChanged()) {
                c.setReportHeader(c.getReportHeader() + ("\n\t\tChanged minLengthExclusive from " + c.oldNode.getMinExclusive() + " to " + c.newNode.getMinExclusive()));
            }
            if (c.isTotalDigitsChanged()) {
                c.setReportHeader(c.getReportHeader() + ("\n\t\tChanged totalDigits from " + c.oldNode.getTotalDigits() + " to " + c.newNode.getTotalDigits()));
            }
            if (c.isFractionDigitsChanged()) {
                c.setReportHeader(c.getReportHeader() + ("\n\t\tChanged fractionDigits from " + c.oldNode.getFractionDigits() + " to " + c.newNode.getFractionDigits()));
            }
            if (c.isEnumerationChanged()) {
                c.setReportHeader(c.getReportHeader() + "\n\t\tChanged enumeration from " + c.oldNode.getEnumeration() + " to " + c.newNode.getEnumeration());
                ArrayList<String> newEnumerationList = new ArrayList<>(c.newNode.getEnumeration());
                boolean isFixedDefaultByEnumeration = newEnumerationList.size() == 1;
                if(c.oldNode.getEnumeration() != null) {
                    newEnumerationList.removeAll(c.oldNode.getEnumeration());
                }
                if(c.oldNode.getEnumeration() == null || newEnumerationList.size() > 0){
                    c.setReportHeader(c.getReportHeader() + "\n\t\t\tadded: " + newEnumerationList.toString());
                    if(isFixedDefaultByEnumeration) {// if single enumeration (new)
                        if (c.oldNode.getFixedValue() != null && c.oldNode.getFixedValue().equals(newEnumerationList.get(0))) {
                            c.setReportHeader(c.getReportHeader() + (" (no semantic change, as new single enumeration value existed as previous fixed default: " + c.newNode.getFixedValue() + ")"));
                        }
                    }
                }else{
                    ArrayList<String> oldEnumerationList = new ArrayList<>(c.oldNode.getEnumeration());
                    boolean wasFixedDefaultByEnumeration = oldEnumerationList.size() == 1;
                    oldEnumerationList.removeAll(c.newNode.getEnumeration());
                    c.setReportHeader(c.getReportHeader() + "\n\t\t\tremoved: " + oldEnumerationList.toString());
                    if(wasFixedDefaultByEnumeration){ // if was a single neumeration (old)
                        if (c.newNode.getFixedValue() != null && c.newNode.getFixedValue().equals(oldEnumerationList.get(0))) {
                            c.setReportHeader(c.getReportHeader() + (" (no semantic change, as removed single enumeration value still exists as fixed default: " + c.newNode.getFixedValue() + ")"));
                        }
                    }
                }
            }
            if (c.isWhitespaceChanged()) {
                c.setReportHeader(c.getReportHeader() + ("\n\t\tChanged whitespace from " + c.oldNode.getWhitespace() + " to " + c.newNode.getWhitespace()));
            }
            if (c.isCompositorChanged()) {
                c.setReportHeader(c.getReportHeader() + ("\n\t\tChanged compositor from " + XsdNode.CompositorType.valueOf(c.oldNode.getCompositor())  + " to " + XsdNode.CompositorType.valueOf(c.newNode.getCompositor())));
            }
        } else if (c.type == ChangeType.REMOVED) {
            if (c.isElement) {
                c.setReportHeader("Removed element <" + c.oldNode.getName() + "> {" + c.oldNode.getTypeName() + "}{" + c.oldNode.getCardinality() + "} in type {" + c.oldNode.getParent().getTypeName() + "}");
                c.setReportBody("in " + c.oldNode.getParent().getName() + " at " + c.oldNode.getXPath());
            } else {
                c.setReportHeader("Removed attribute @" + c.oldNode.getName() + " {" + c.oldNode.getTypeName() + "}{" + c.oldNode.getCardinality() + "} in type {" + c.oldNode.getParent().getTypeName() + "}");
                c.setReportBody("in " + c.oldNode.getParent().getName() + " at " + c.oldNode.getXPath());
            }
        }
    }

    public void addChange(Change c){
        changes.add(c);
    }


    /** Report items will be given out to console in the end.
     * There is usually a key line (like an element was removed) and the value lines might differ (the parent element with its XPATH) */
    private void createMessage(Change c){

        createChangeMessage(c);
        String reportHeader = c.getReportHeader();
        Set<String> bodyLines;
        if(!report.containsKey(reportHeader)){
            bodyLines = new TreeSet<>();
            updateStatistic(c, true);
        }else{
            bodyLines = report.get(reportHeader);
            updateStatistic(c, false);
        }
        bodyLines.add(c.getReportBody());
        report.put(reportHeader, bodyLines);
    }
    private void createMessages(){
        for(Change c : changes){
            createMessage(c);
        }
    }

    public String getReport(){
        createMessages();
        StringBuilder output = new StringBuilder();
        output.append(getReportHeader());
        for(String headerLine : this.report.keySet()){
            output.append(headerLine).append("\n");
            Set<String> bodyLines = report.get(headerLine);
            for(String bodyLine : bodyLines){
                output.append("\t").append(bodyLine).append("\n");
            }
            output.append("\n");
        }
        output.append("\n");
        output.append(getReportFooter());
        return output.toString();
    }

    public void reset() {
        this.removedElementsInTypes = 0;
        this.removedElementsInXML = 0;
        this.removedAttributesInTypes = 0;
        this.removedAttributesInXML = 0;
        this.addedAttributesInTypes = 0;
        this.addedAttributesInXML = 0;
        this.addedElementsInTypes = 0;
        this.addedElementsInXML = 0;
        this.report.clear();
        this.changes.clear();
        reportHeader = "";
        reportFooter = null;
    }
}
