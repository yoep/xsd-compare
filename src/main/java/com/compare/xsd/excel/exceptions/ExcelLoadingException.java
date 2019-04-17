package com.compare.xsd.excel.exceptions;

import lombok.Getter;

import java.io.File;

@Getter
public class ExcelLoadingException extends RuntimeException {
    private final File file;

    public ExcelLoadingException(File file, Throwable cause) {
        super("Excel file " + file + " couldn't be loaded", cause);
        this.file = file;
    }
}
