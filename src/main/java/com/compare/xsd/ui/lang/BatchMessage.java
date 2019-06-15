package com.compare.xsd.ui.lang;

import lombok.Getter;

@Getter
public enum BatchMessage implements Message {
    TITLE("batch_title");

    private String key;

    BatchMessage(String key) {
        this.key = key;
    }
}
