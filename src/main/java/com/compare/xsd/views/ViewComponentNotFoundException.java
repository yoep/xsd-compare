package com.compare.xsd.views;

public class ViewComponentNotFoundException extends RuntimeException {
    public ViewComponentNotFoundException(String selector) {
        super("View node not found " + selector);
    }
}
