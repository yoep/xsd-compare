package com.compare.xsd.model.comparison;

import com.compare.xsd.model.xsd.XsdNode;
import lombok.Data;

@Data
public class Modifications {
    private ModificationType type;

    private boolean nameChanged;
    private boolean typeChanged;
    private boolean cardinalityChanged;

    /**
     * Initialize a new instance of {@link Modifications}.
     *
     * @param type Set the type of modifications.
     */
    public Modifications(ModificationType type) {
        this.type = type;
    }

    /**
     * Verify if modifications were made.
     *
     * @param compareNode Set the node which was used for comparison.
     */
    public void verify(XsdNode compareNode) {
        if (type == ModificationType.NONE) {
            if (nameChanged || typeChanged || cardinalityChanged) {
                type = ModificationType.MODIFIED;
                compareNode.setModifications(this);
            }
        }
    }
}
