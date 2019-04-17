package com.compare.xsd.managers;

public class PrimaryWindowAlreadyPresentException extends RuntimeException {
    public PrimaryWindowAlreadyPresentException() {
        super("Primary window is already present and cannot be added again");
    }
}
