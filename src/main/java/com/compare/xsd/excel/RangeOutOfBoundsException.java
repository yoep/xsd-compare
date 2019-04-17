package com.compare.xsd.excel;

public class RangeOutOfBoundsException extends InvalidCellRangeException {
    public RangeOutOfBoundsException(String range) {
        super("Cell range " + range + " is out of bounds");
        this.range = range;
    }
}
