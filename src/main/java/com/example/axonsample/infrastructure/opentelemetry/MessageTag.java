package com.example.axonsample.infrastructure.opentelemetry;

public enum MessageTag {
    MESSAGE_ID("axon.message.id"),
    AGGREGATE_ID("axon.message.aggregate-identifier"),
    MESSAGE_TYPE("axon.message.type"),
    PAYLOAD_TYPE("axon.message.payload-type"),
    MESSAGE_NAME("axon.message.message-name"),
    PAYLOAD("axon.message.payload");

    private final String tagKey;

    MessageTag(String tagName) {
        this.tagKey = tagName;
    }

    public String getTagKey() {
        return tagKey;
    }
}
