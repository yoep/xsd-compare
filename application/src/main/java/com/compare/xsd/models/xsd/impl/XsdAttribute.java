package com.compare.xsd.models.xsd.impl;

import com.compare.xsd.models.xsd.XsdNode;
import javafx.scene.image.Image;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@EqualsAndHashCode
@Getter
public class XsdAttribute implements XsdNode {
    private String name;
    private String type;

    //region Getters & Setters

    @Override
    public String getCardinality() {
        return null;
    }

    @Override
    public Image getIcon() {
        return new Image(getClass().getResourceAsStream("/icons/attribute.png"));
    }

    //endregion
}
