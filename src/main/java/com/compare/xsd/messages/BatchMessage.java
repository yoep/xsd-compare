package com.compare.xsd.messages;

import com.github.spring.boot.javafx.text.Message;
import lombok.Getter;

@Getter
public enum BatchMessage implements Message {
    TITLE("batch_title");

    private String key;

    BatchMessage(String key) {
        this.key = key;
    }
}
