package com.compare.xsd.excel;

import lombok.Getter;

/**
 * Defines that the given name exceeds to limitation.
 */
@Getter
public class NameTooLongException extends RuntimeException {
    private final String name;
    private final int limit;

    public NameTooLongException(String name, int limit) {
        super(name + " exceeds the maximum limit of " + limit);
        this.name = name;
        this.limit = limit;
    }
}
