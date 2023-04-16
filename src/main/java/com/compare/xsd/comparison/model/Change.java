package com.compare.xsd.comparison.model;

import com.compare.xsd.comparison.model.xsd.XsdNode;
import com.compare.xsd.comparison.model.xsd.impl.AbstractXsdNode;
import com.compare.xsd.comparison.model.xsd.impl.XsdAttribute;
import com.compare.xsd.comparison.model.xsd.impl.XsdElement;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor(force = true)
public class Change {
    public AbstractXsdNode newNode;
    public AbstractXsdNode oldNode;
    public ChangeType type;
    public boolean isElement;

    private boolean nameChanged;
    private boolean namespaceChanged;
    private boolean typeChanged;
    private boolean cardinalityChanged;
    private boolean fixedDefaultChanged;
    private boolean lengthChanged;
    private boolean maxLengthChanged;
    private boolean maxExclusiveChanged;
    private boolean maxInclusiveChanged;
    private boolean minExclusiveChanged;
    private boolean minInclusiveChanged;
    private boolean totalDigitsChanged;
    private boolean fractionDigitsChanged;
    private boolean minLengthChanged;
    private boolean patternChanged;
    private boolean enumerationChanged;
    private boolean whitespaceChanged;

    String reportHeader;
    String reportBody;

    //region Constructors

    /**
     * Initialize a new instance of {@link Change}.
     *
     * @param type Set the type of modifications.
     */
    public Change(ChangeType type) {
        this.type = type;
    }
    public Change(ChangeType type, XsdNode oldNode, XsdNode newNode) {
        this.type = type;
        if(oldNode instanceof XsdAttribute || newNode instanceof XsdAttribute){
            assert(newNode == null || newNode instanceof XsdAttribute);
            isElement = false;
            this.oldNode = (AbstractXsdNode) oldNode;
            this.newNode = (AbstractXsdNode) newNode;
        }else if(oldNode instanceof XsdElement || newNode instanceof XsdElement){
            assert(newNode == null || newNode instanceof XsdElement);
                isElement = true;
                this.oldNode = (AbstractXsdNode) oldNode;
                this.newNode = (AbstractXsdNode) newNode;
        }
        // different document file names (document as XsdNode input at all)  will not result into a modification object
        // as identical grammars with different names should be returned as equal!
    }
    //endregion

    //region Getters & Setters

    /**
     * Verify if any modifications were made.
     *
     * @return Returns true if something changed, else false.
     */
    public boolean isModified() {
        return nameChanged || namespaceChanged || typeChanged || cardinalityChanged || fixedDefaultChanged || lengthChanged || maxLengthChanged ||
                minLengthChanged || patternChanged || enumerationChanged || whitespaceChanged || maxExclusiveChanged  || maxInclusiveChanged  || minExclusiveChanged  || minInclusiveChanged  || totalDigitsChanged  || fractionDigitsChanged;
    }

    //endregion
}
