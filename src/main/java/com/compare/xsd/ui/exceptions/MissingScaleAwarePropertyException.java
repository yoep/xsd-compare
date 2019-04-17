package com.compare.xsd.ui.exceptions;

/**
 * Defines the exception that the scale aware scene is missing from the controller at initialization.
 */
public class MissingScaleAwarePropertyException extends RuntimeException {
    public MissingScaleAwarePropertyException() {
        super("Missing scene to execute scale aware logic");
    }
}
