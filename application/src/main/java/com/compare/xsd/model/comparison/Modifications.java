package com.compare.xsd.model.comparison;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class Modifications {
    private ModificationType type;

    private boolean nameChanged;
    private boolean typeChanged;
    private boolean cardinalityChanged;
    private boolean fixedValueChanged;
    private boolean lengthChanged;
    private boolean maxLengthChanged;
    private boolean minLengthChanged;
    private boolean patternChanged;
    private boolean enumerationChanged;
    private boolean whitespaceChanged;

    //region Constructors

    /**
     * Initialize a new instance of {@link Modifications}.
     *
     * @param type Set the type of modifications.
     */
    public Modifications(ModificationType type) {
        this.type = type;
    }

    //endregion

    //region Getters & Setters

    /**
     * Verify if any modifications were made.
     *
     * @return Returns true if something changed, else false.
     */
    public boolean isModified() {
        return nameChanged || typeChanged || cardinalityChanged || fixedValueChanged || lengthChanged || maxLengthChanged || minLengthChanged ||
                patternChanged || enumerationChanged || whitespaceChanged;
    }

    //endregion
}
