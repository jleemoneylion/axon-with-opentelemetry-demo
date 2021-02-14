package com.example.axonsample.infrastructure.opentelemetry;

import io.opentelemetry.context.propagation.TextMapPropagator;
import org.axonframework.messaging.Message;

import java.util.HashMap;
import java.util.Map;

public class MapInjector implements TextMapPropagator.Setter<Message<?>> {
    private final Map<String, String> metadata = new HashMap<>();

    @Override
    public void set(Message<?> carrier, String key, String value) {
        metadata.put(key, value);
    }

    public Map<String, String> getMetadata() {
        return metadata;
    }
}
