package com.compare.xsd.model.comparison;

import lombok.Data;

@Data
public class Modifications {
    private ModificationType type;

    private boolean nameChanged;
    private boolean typeChanged;

    /**
     * Initialize a new instance of {@link Modifications}.
     *
     * @param type Set the type of modifications.
     */
    public Modifications(ModificationType type) {
        this.type = type;
    }
}
