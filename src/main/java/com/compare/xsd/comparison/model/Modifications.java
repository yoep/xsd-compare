package com.compare.xsd.comparison.model;

import com.compare.xsd.comparison.model.xsd.XsdNode;
import com.compare.xsd.comparison.model.xsd.impl.AbstractXsdNode;
import com.compare.xsd.comparison.model.xsd.impl.XsdAttribute;
import com.compare.xsd.comparison.model.xsd.impl.XsdElement;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor(force = true)
public class Modifications {
    AbstractXsdNode newNode;
    AbstractXsdNode oldNode;
    private ModificationType type;
    private boolean isElement;

    private boolean nameChanged;
    private boolean namespaceChanged;
    private boolean typeChanged;
    private boolean cardinalityChanged;
    private boolean fixedValueChanged;
    private boolean lengthChanged;
    private boolean maxLengthChanged;
    private boolean minLengthChanged;
    private boolean patternChanged;
    private boolean enumerationChanged;
    private boolean whitespaceChanged;



    private StringBuilder sb;
    //region Constructors

    /**
     * Initialize a new instance of {@link Modifications}.
     *
     * @param type Set the type of modifications.
     */
    public Modifications(ModificationType type) {
        this.type = type;
    }
    public Modifications(XsdNode oldNode, XsdNode newNode) {
        if(oldNode instanceof XsdElement){
            assert(newNode instanceof XsdElement);
            isElement = true;
            this.oldNode = (AbstractXsdNode) oldNode;
            this.newNode = (AbstractXsdNode) newNode;
        } else if(oldNode instanceof XsdAttribute){
            assert(newNode instanceof XsdAttribute);
            isElement = false;
            this.oldNode = (AbstractXsdNode) oldNode;
            this.newNode = (AbstractXsdNode) newNode;
        }
        // different document file names (document as XsdNode input at all)  will not result into a modification object
        // as identical grammars with different names should be returned as equal!
    }
    //endregion

    //region Getters & Setters

    private void init(){
        sb = new StringBuilder();
        this.type = ModificationType.MODIFIED;
        sb.append("Modifying ").append(isElement ? "element: " : "attribute: ").append(
            "\n\told: " + (isElement ? "<" + oldNode.getName() + ">" : "@" + oldNode.getName()) + "{" + oldNode.getNextTypeName() + "} in type {" + oldNode.getParent().getNextTypeName() + "}" +
            "\n\tnew: " + (isElement ? "<" + newNode.getName() + ">" : "@" + newNode.getName()) + "{" + newNode.getNextTypeName() + "} in type {" + newNode.getParent().getNextTypeName() + "}");
    }

    public StringBuilder getStringBuilder(){
        if(sb == null){
            init();
        }
        return sb;
    }
    /**
     * Verify if any modifications were made.
     *
     * @return Returns true if something changed, else false.
     */
    public boolean isModified() {
        return nameChanged || namespaceChanged || typeChanged || cardinalityChanged || fixedValueChanged || lengthChanged || maxLengthChanged ||
                minLengthChanged || patternChanged || enumerationChanged || whitespaceChanged;
    }

    public String getHeaderLine() {
        if(sb == null){
            System.out.println("XXX");
        }
        return sb.toString();
    }

    //endregion
}
