package com.compare.xsd.model.xsd;

import lombok.Getter;

@Getter
public class NodeNotFoundException extends RuntimeException {
    private String name;

    public NodeNotFoundException(String name) {
        super("Node couldn't be found with name '" + name + "'");
        this.name = name;
    }
}
