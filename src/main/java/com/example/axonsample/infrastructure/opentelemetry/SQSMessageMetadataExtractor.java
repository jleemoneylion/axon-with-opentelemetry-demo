package com.example.axonsample.infrastructure.opentelemetry;

import io.opentelemetry.context.propagation.TextMapPropagator;
import software.amazon.awssdk.services.sqs.model.Message;

import java.util.Map;

public class SQSMessageMetadataExtractor implements TextMapPropagator.Getter<Message> {
    private final Map<String, String> metadata;

    public SQSMessageMetadataExtractor(Map<String, String> metadata) {
        this.metadata = metadata;
    }

    @Override
    public Iterable<String> keys(Message message) {
        return metadata.keySet();
    }

    @Override
    public String get(Message message, String key) {
        return metadata.get(key);
    }
}
