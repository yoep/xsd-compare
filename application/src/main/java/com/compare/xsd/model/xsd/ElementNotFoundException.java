package com.compare.xsd.model.xsd;

import lombok.Getter;

@Getter
public class ElementNotFoundException extends RuntimeException {
    private String name;

    public ElementNotFoundException(String name) {
        super("Element couldn't be found with name '" + name + "'");
        this.name = name;
    }
}
