package com.example.axonsample.infrastructure.opentelemetry;

import io.opentelemetry.context.propagation.TextMapPropagator;
import org.axonframework.messaging.Message;

public class AxonMessageMapExtractor implements TextMapPropagator.Getter<Message<?>> {
    @Override
    public Iterable<String> keys(Message<?> message) {
        return message.getMetaData().keySet();
    }

    @Override
    public String get(Message<?> message, String key) {
        if (message == null)
            return null;

        return (String) message.getMetaData().get(key);
    }
}
