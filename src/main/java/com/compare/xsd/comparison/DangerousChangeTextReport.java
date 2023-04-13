package com.compare.xsd.comparison;

import com.compare.xsd.comparison.model.Change;
import com.compare.xsd.comparison.model.ChangeType;
import com.compare.xsd.comparison.model.xsd.impl.XsdAttribute;
import com.compare.xsd.comparison.model.xsd.impl.XsdDocument;
import lombok.Data;

import java.util.*;

/** The SingleLineChangeTextReport creates only a single text line for every XSD change
 * this harder to read, but assists  to compare the result with other XSD comparison tools */
@Data
public class DangerousChangeTextReport implements TextReport {

    private StringBuilder sb;
    /** creating a report with a header line and a set of body lines */
    private Set<String> report = new TreeSet<>();
    private List<Change> changes = new ArrayList<>();

    public void addDocuments(XsdDocument oldNode, XsdDocument newNode){}

    /**
     1) ADDING
     /CrossIndustryInvoice/@languageID added @languageID {token}{0..1} in type {CodeType}
     /CrossIndustryInvoice/BasisDateTime added <BasisDateTime> {DateTimeType}{0..1} in type {TradePriceType}

     2) MODIFYING
     /CrossIndustryInvoice/@format modifying attribute: old: @format{FormattedDateTimeFormatContentType}{0..1} in type {FormattedDateTimeType} new: @format{TimePointFormatCodeContentType}{0..1} in type {FormattedDateTimeType} Changed type namespace from urn:un:unece:uncefact:data:standard:QualifiedDataType:100 to urn:un:unece:uncefact:codelist:standard:UNECE:TimePointFormatCode:D21B Changed type from FormattedDateTimeFormatContentType to TimePointFormatCodeContentType
     /CrossIndustryInvoice/AssociatedDocumentLineDocument modifying element: old: <AssociatedDocumentLineDocument>{DocumentLineDocumentType}{1..1} in type {SupplyChainTradeLineItemType} new: <AssociatedDocumentLineDocument>{DocumentLineDocumentType}{0..1} in type {SupplyChainTradeLineItemType} Changed cardinality from 1..1 to 0..1

     3) REMOVAL
     /CrossIndustryInvoice/@format removed @format {string}{0..1} in type {NumericType}
     /CrossIndustryInvoice/SubordinateBasicWorkItem <SubordinateBasicWorkItem> {BasicWorkItemType}{0..*} in type {BasicWorkItemType}

     */
    private void createChangeMessage(Change c){
        if (c.type == ChangeType.ADDED && c.newNode.getCardinality().startsWith("1..")) {
            /*
             *      1) ADDING
             *      /CrossIndustryInvoice/BasisDateTime added <BasisDateTime> {DateTimeType}{0..1} in type {TradePriceType}
             *      /CrossIndustryInvoice/@languageID added @languageID {token}{0..1} in type {CodeType}
             */
            if (c.isElement) {
                c.setReportHeader("In type {" + c.newNode.getParent().getTypeName() + "} added <" + c.newNode.getName() + "> {" + c.newNode.getTypeName() + "}{" + c.newNode.getCardinality() + "} " + c.newNode.getXPath());
            } else {
                c.setReportHeader("In type {" + c.newNode.getParent().getTypeName() + "} added @" + c.newNode.getName() + " {" + c.newNode.getTypeName() + "}{" + c.newNode.getCardinality() + "} " + c.newNode.getXPath());
            }
        } else if (c.type == ChangeType.MODIFIED && c.oldNode.getCardinality().startsWith("0..") && c.newNode.getCardinality().startsWith("1..")) {
            /*
             *      2) MODIFYING
             *      /CrossIndustryInvoice/@format modifying attribute: old: @format{FormattedDateTimeFormatContentType}{0..1} in type {FormattedDateTimeType} new: @format{TimePointFormatCodeContentType}{0..1} in type {FormattedDateTimeType} Changed type namespace from urn:un:unece:uncefact:data:standard:QualifiedDataType:100 to urn:un:unece:uncefact:codelist:standard:UNECE:TimePointFormatCode:D21B Changed type from FormattedDateTimeFormatContentType to TimePointFormatCodeContentType
             *      /CrossIndustryInvoice/AssociatedDocumentLineDocument modifying element: old: <AssociatedDocumentLineDocument>{DocumentLineDocumentType}{1..1} in type {SupplyChainTradeLineItemType} new: <AssociatedDocumentLineDocument>{DocumentLineDocumentType}{0..1} in type {SupplyChainTradeLineItemType} Changed cardinality from 1..1 to 0..1
             */
        c.setReportHeader("In type {" + c.newNode.getParent().getNextTypeName() + "}" + c.newNode.getXPath() + " modifying " + (c.isElement ? "element: " : "attribute: ") +
                "old: " + (c.isElement ? "<" + c.oldNode.getName() + ">" : "@" + c.oldNode.getName()) + "{" + c.oldNode.getNextTypeName() + "}{" + c.oldNode.getCardinality() + "} in type {" + c.oldNode.getParent().getNextTypeName() + "}" +
                "new: " + (c.isElement ? "<" + c.newNode.getName() + ">" : "@" + c.newNode.getName()) + "{" + c.newNode.getNextTypeName() + "}{" + c.newNode.getCardinality() + "} in type {" + c.newNode.getParent().getNextTypeName() + "}");
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
                c.setReportHeader(c.getReportHeader() + " changed type namespace from " + c.oldNode.getTypeNamespace() + " to " + c.newNode.getTypeNamespace());
            }
            if (c.isTypeChanged()) {
                c.setReportHeader(c.getReportHeader() + (" changed type from " + c.oldNode.getTypeName() + " to " + c.newNode.getTypeName()));
            }
            if (c.isCardinalityChanged()) {
                c.setReportHeader(c.getReportHeader() + (" changed cardinality from " + c.oldNode.getCardinality() + " to " + c.newNode.getCardinality()));
            }
            if (c.isFixedDefaultChanged()) {
                c.setReportHeader(c.getReportHeader() + (" changed fixed default from " + ((XsdAttribute) c.oldNode).getFixedDefaultValue() + " to " + ((XsdAttribute) c.newNode).getFixedDefaultValue()));
            }
            if (c.isPatternChanged()) {
                c.setReportHeader(c.getReportHeader() + (" changed pattern from " + c.oldNode.getPattern() + " to " + c.newNode.getPattern()));
            }
            if (c.isMaxLengthChanged()) {
                c.setReportHeader(c.getReportHeader() + (" changed maxLength from " + c.oldNode.getMaxLength() + " to " + c.newNode.getMaxLength()));
            }
            if (c.isMinLengthChanged()) {
                c.setReportHeader(c.getReportHeader() + (" changed minLength from " + c.oldNode.getMinLength() + " to " + c.newNode.getMinLength()));
            }
            if (c.isLengthChanged()) {
                c.setReportHeader(c.getReportHeader() + (" changed length from " + c.oldNode.getLength() + " to " + c.newNode.getLength()));
            }
            if (c.isEnumerationChanged()) {
                c.setReportHeader(c.getReportHeader() + (" changed enumeration from " + c.oldNode.getEnumeration() + " to " + c.newNode.getEnumeration()));
            }
            if (c.isWhitespaceChanged()) {
                c.setReportHeader(c.getReportHeader() + (" changed whitespace from " + c.oldNode.getWhitespace() + " to " + c.newNode.getWhitespace()));
            }
        } else if (c.type == ChangeType.REMOVED) {
            /*
             *      3) REMOVAL
             *      /CrossIndustryInvoice/SubordinateBasicWorkItem <SubordinateBasicWorkItem> {BasicWorkItemType}{0..*} in type {BasicWorkItemType}
             *      /CrossIndustryInvoice/@format removed @format {string}{0..1} in type {NumericType}             *
             */
            if (c.isElement) {
                c.setReportHeader("In type {" + c.oldNode.getParent().getTypeName() + "}" + c.oldNode.getXPath() + " removed <" + c.oldNode.getName() + "> {" + c.oldNode.getTypeName() + "}{" + c.oldNode.getCardinality() + "}");
            } else {
                c.setReportHeader("In type {" + c.oldNode.getParent().getTypeName() + "}" + c.oldNode.getXPath() + " removed @" + c.oldNode.getName() + " {" + c.oldNode.getTypeName() + "}{" + c.oldNode.getCardinality() + "}");
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
        if(reportHeader != null){
            report.add(reportHeader);
        }
    }

    private void createMessages(){
        for(Change c : changes){
            createMessage(c);
        }
    }

    public String getReport(){
        createMessages();
        StringBuilder output = new StringBuilder();
        for(String headerLine : report){
            output.append(headerLine).append("\n");
        }
        return output.toString();
    }

    public void reset() {
        this.report.clear();
        this.changes.clear();
    }
}
