package com.compare.xsd.comparison;

import javafx.scene.Node;
import lombok.Getter;

@Getter
public class UnknownNodeException extends RuntimeException {
    private Node node;

    public UnknownNodeException(Node node) {
        super("Couldn't find matching element for " + node);
        this.node = node;
    }
}
