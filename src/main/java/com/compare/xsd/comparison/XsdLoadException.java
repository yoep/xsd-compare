package com.compare.xsd.comparison;

/**
 * Signals that the loading of an XSD file has failed.
 */
public class XsdLoadException extends RuntimeException {
    public XsdLoadException(String message) {
        super("Failed to load XSD file, " + message);
    }

    public XsdLoadException(String message, Throwable cause) {
        super("Failed to load XSD file, " + message, cause);
    }
}
