package com.compare.xsd.excel.exceptions;

import lombok.Getter;

@Getter
public class InvalidCellRangeException extends RuntimeException {
    protected String range;

    public InvalidCellRangeException(String range) {
        super("Excel range " + range + " is invalid");
        this.range = range;
    }
}
