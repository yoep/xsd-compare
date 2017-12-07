package com.compare.xsd.managers;

public class PrimaryWindowNotAvailableException extends Exception {
    public PrimaryWindowNotAvailableException() {
        super("Primary window is not available.");
    }
}
