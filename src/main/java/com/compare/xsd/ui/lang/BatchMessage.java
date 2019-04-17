package com.compare.xsd.ui.lang;

public enum BatchMessage implements Message {
    TITLE("batch_title");

    private String key;

    BatchMessage(String key) {
        this.key = key;
    }

    @Override
    public String getKey() {
        return key;
    }
}
