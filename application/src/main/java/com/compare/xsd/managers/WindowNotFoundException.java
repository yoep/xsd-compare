package com.compare.xsd.managers;

import lombok.Getter;

@Getter
public class WindowNotFoundException extends Exception {
    private final String name;

    public WindowNotFoundException(String name) {
        super("Window '" + name + "' couldn't be found");
        this.name = name;
    }
}
