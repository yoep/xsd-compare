package com.compare.xsd.ui;

public class ActionCancelledException extends Exception {
    public ActionCancelledException() {
        super("Action has been cancelled by the user");
    }
}
