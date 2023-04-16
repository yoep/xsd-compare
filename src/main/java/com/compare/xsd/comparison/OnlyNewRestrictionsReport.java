package com.compare.xsd.comparison;

import com.compare.xsd.comparison.model.Change;
import com.compare.xsd.comparison.model.ChangeType;
import com.compare.xsd.comparison.model.xsd.impl.XsdAttribute;
import com.compare.xsd.comparison.model.xsd.impl.XsdDocument;
import lombok.Data;

import java.util.*;

/**
 * The purpose of this report shows if the new version provides new restrictions, requirements or limitations in comparison to the old grammar.
 * For instance, if the old grammar is a subset of the new grammar, the new grammar should not provide any new restrictions, requirements or limitations!
 *
 * The OnlyNewRestrictionsReport creates only a single text line for every XSD change sorted by Type
 * this harder to read, but assists  to compare the result with other XSD comparison tools
 *
 * 1) Any Removal is a new restriction.
 * 2) Every Modification that limits the possible values
 * 3) If there is a new XML node which did not previously existed but is now mandatory on the elements of the old grammar (being the subset)!
 *
 * */
@Data
public class OnlyNewRestrictionsReport implements TextReport {

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
                c.setReportHeader("In type {" + c.newNode.getParent().getTypeName() + "} added <" + c.newNode.getName() + "> {" + c.newNode.getTypeName() + "}{" + c.newNode.getCardinality() + "} "); // + c.newNode.getXPath());
            } else {
                c.setReportHeader("In type {" + c.newNode.getParent().getTypeName() + "} added @" + c.newNode.getName() + " {" + c.newNode.getTypeName() + "}{" + c.newNode.getCardinality() + "} "); // + c.newNode.getXPath());
            }
        } else if (c.type == ChangeType.MODIFIED) {
            /*
             *      2) MODIFYING
             *      /CrossIndustryInvoice/@format modifying attribute: old: @format{FormattedDateTimeFormatContentType}{0..1} in type {FormattedDateTimeType} new: @format{TimePointFormatCodeContentType}{0..1} in type {FormattedDateTimeType} Changed type namespace from urn:un:unece:uncefact:data:standard:QualifiedDataType:100 to urn:un:unece:uncefact:codelist:standard:UNECE:TimePointFormatCode:D21B Changed type from FormattedDateTimeFormatContentType to TimePointFormatCodeContentType
             *      /CrossIndustryInvoice/AssociatedDocumentLineDocument modifying element: old: <AssociatedDocumentLineDocument>{DocumentLineDocumentType}{1..1} in type {SupplyChainTradeLineItemType} new: <AssociatedDocumentLineDocument>{DocumentLineDocumentType}{0..1} in type {SupplyChainTradeLineItemType} Changed cardinality from 1..1 to 0..1
             */

        /*  Example of complete list:
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
            /**
            if (c.isNamespaceChanged()) {
                c.setReportHeader(c.getReportHeader() + "\n\t\tChanged type namespace from " + c.oldNode.getTypeNamespace() + " to " + c.newNode.getTypeNamespace());
            }
            if (c.isTypeChanged()) {
                c.setReportHeader(c.getReportHeader() + ("\n\t\tChanged type from " + c.oldNode.getTypeName() + " to " + c.newNode.getTypeName()));
            }**/
            if (c.isCardinalityChanged()) {
                if(c.oldNode.getMinOccurrence() < c.newNode.getMinOccurrence() || c.oldNode.getMaxOccurrence() == null && c.newNode.getMaxOccurrence() != null ||  c.oldNode.getMaxOccurrence() != null && c.newNode.getMaxOccurrence() != null && c.oldNode.getMaxOccurrence() > c.newNode.getMaxOccurrence()){
                    getModificationStringBuilder().append("\n\t\tRestricted cardinality from " + c.oldNode.getCardinality() + " to " + c.newNode.getCardinality());
                }
            }
            if (c.isFixedDefaultChanged()) {
                getModificationStringBuilder().append("\n\t\tChanged fixed default from " + ((XsdAttribute) c.oldNode).getFixedDefaultValue() + " to " + ((XsdAttribute) c.newNode).getFixedDefaultValue());            }
            if (c.isPatternChanged()) {
                getModificationStringBuilder().append("\n\t\tChanged pattern from " + c.oldNode.getPattern() + " to " + c.newNode.getPattern());
            }
            if (c.isMinLengthChanged()) {
                // if there was no facet. but now it is a restriction
                if (c.oldNode.getMinLength() == null && c.newNode.getMinLength() != null ||
                        // or if the new min value is bigger it is a restriction
                c.newNode.getMinLength() != null && c.oldNode.getMinLength() != null && (c.oldNode.getMinLength() < c.newNode.getMinLength())) {
                    getModificationStringBuilder().append("\n\t\tChanged minLength from " + c.oldNode.getMinLength() + " to " + c.newNode.getMinLength());
                }
            }if (c.isMaxLengthChanged()){
                // if there was no facet. but now it is a restriction
                if (c.oldNode.getMaxLength() == null && c.newNode.getMaxLength() != null ||
                        // or if the new max value is smaller it is a restriction
                        c.newNode.getMaxLength() != null && c.oldNode.getMaxLength() != null && (c.oldNode.getMaxLength() > c.newNode.getMaxLength())) {
                    getModificationStringBuilder().append("\n\t\tChanged maxLength from " + c.oldNode.getMaxLength() + " to " + c.newNode.getMaxLength());
                }
            }
            if (c.isMaxInclusiveChanged()) {
                // if there was no facet. but now it is a restriction
                if (c.oldNode.getMaxInclusive() == null && c.newNode.getMaxInclusive() != null ||
                        // or if the new max value is smaller it is a restriction
                        c.newNode.getMaxInclusive() != null && (c.oldNode.getMaxInclusive() != null && (c.oldNode.getMaxInclusive() > c.newNode.getMaxInclusive()))) {
                    getModificationStringBuilder().append("\n\t\tChanged maxInclusive from " + c.oldNode.getMaxInclusive() + " to " + c.newNode.getMaxInclusive());
                }
            }
            if (c.isMaxExclusiveChanged()) {
                // if there was no facet. but now it is a restriction
                if (c.oldNode.getMaxExclusive() == null && c.newNode.getMaxExclusive() != null ||
                        // or if the new max value is smaller it is a restriction
                        c.newNode.getMaxExclusive() != null && (c.oldNode.getMaxExclusive() != null && (c.oldNode.getMaxExclusive() > c.newNode.getMaxExclusive()))) {
                    getModificationStringBuilder().append("\n\t\tChanged maxExclusive from " + c.oldNode.getMaxExclusive() + " to " + c.newNode.getMaxExclusive());
                }
            }
            if (c.isMinInclusiveChanged()) {
                // if there was no facet. but now it is a restriction
                if (c.oldNode.getMinInclusive() == null && c.newNode.getMinInclusive() != null ||
                    // or if the new min value is bigger it is a restriction
                    c.newNode.getMinInclusive() != null && c.oldNode.getMinInclusive() != null && (c.oldNode.getMinInclusive() < c.newNode.getMinInclusive())) {
                    getModificationStringBuilder().append("\n\t\tChanged minInclusive from " + c.oldNode.getMinInclusive() + " to " + c.newNode.getMinInclusive());
                }
            }
            if (c.isMinExclusiveChanged()) {
                // if there was no facet. but now it is a restriction
                if (c.oldNode.getMinExclusive() == null && c.newNode.getMinExclusive() != null ||
                // or if the new min value is bigger it is a restriction
                    c.newNode.getMinExclusive() != null && c.oldNode.getMinExclusive() != null && (c.oldNode.getMinExclusive() < c.newNode.getMinExclusive())) {
                    getModificationStringBuilder().append("\n\t\tChanged minExclusive from " + c.oldNode.getMinExclusive() + " to " + c.newNode.getMinExclusive());
                }
            }
            if (c.isTotalDigitsChanged()) {
                getModificationStringBuilder().append("\n\t\tChanged totalDigits from " + c.oldNode.getTotalDigits() + " to " + c.newNode.getTotalDigits());
            }
            if (c.isFractionDigitsChanged()) {
                getModificationStringBuilder().append("\n\t\tChanged fractionDigits from " + c.oldNode.getFractionDigits() + " to " + c.newNode.getFractionDigits());
            }
            if (c.isEnumerationChanged()) {
                ArrayList<String> oldEnumerationList = new ArrayList<>(c.oldNode.getEnumeration());
                oldEnumerationList.removeAll(c.newNode.getEnumeration());
                if(oldEnumerationList.size() > 0){
                    getModificationStringBuilder().append("\n\t\tRestricted enumeration from " + c.oldNode.getEnumeration() + " to " + c.newNode.getEnumeration());
                }
            }
            if (c.isWhitespaceChanged()) {
                getModificationStringBuilder().append("\n\t\tChanged whitespace from " + c.oldNode.getWhitespace() + " to " + c.newNode.getWhitespace());
            }
            String writtenModificdations = getModificationStringBuilder().toString();
            if(writtenModificdations != null && !writtenModificdations.isEmpty()){
                modifications = null;
                c.setReportHeader((c.getReportHeader() != null ? c.getReportHeader() : "") + "In type {" + c.newNode.getParent().getNextTypeName() + "} modifying " + (c.isElement ? "element: " : "attribute: ") +
                        "\n\told: " + (c.isElement ? "<" + c.oldNode.getName() + ">" : "@" + c.oldNode.getName()) + "{" + c.oldNode.getNextTypeName() + "}{" + c.oldNode.getCardinality() + "} in type {" + c.oldNode.getParent().getNextTypeName() + "}" +
                        "\n\tnew: " + (c.isElement ? "<" + c.newNode.getName() + ">" : "@" + c.newNode.getName()) + "{" + c.newNode.getNextTypeName() + "}{" + c.newNode.getCardinality() + "} in type {" + c.newNode.getParent().getNextTypeName() + "}"
                        + writtenModificdations);
            }
        } else if (c.type == ChangeType.REMOVED) {
            /*
             *      3) REMOVAL
             *      /CrossIndustryInvoice/SubordinateBasicWorkItem <SubordinateBasicWorkItem> {BasicWorkItemType}{0..*} in type {BasicWorkItemType}
             *      /CrossIndustryInvoice/@format removed @format {string}{0..1} in type {NumericType}             *
             */
            if (c.isElement) {
                c.setReportHeader("In type {" + c.oldNode.getParent().getTypeName() + "} removed <" + c.oldNode.getName() + "> {" + c.oldNode.getTypeName() + "}{" + c.oldNode.getCardinality() + "}");
            } else {
                c.setReportHeader("In type {" + c.oldNode.getParent().getTypeName() + "} removed @" + c.oldNode.getName() + " {" + c.oldNode.getTypeName() + "}{" + c.oldNode.getCardinality() + "}");
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

    StringBuilder modifications;
    private StringBuilder getModificationStringBuilder(){
        if(modifications == null){
            modifications = new StringBuilder();
        }
        return modifications;
    }
}
