package com.example.axonsample.infrastructure.opentelemetry;

import io.opentelemetry.context.propagation.TextMapPropagator;

import java.util.HashMap;
import java.util.Map;

public class MapInjector implements TextMapPropagator.Setter<Object> {
    private final Map<String, String> metadata = new HashMap<>();

    @Override
    public void set(Object carrier, String key, String value) {
        metadata.put(key, value);
    }

    public Map<String, String> getMetadata() {
        return metadata;
    }
}
