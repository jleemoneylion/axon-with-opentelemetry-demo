package com.example.axonsample.infrastructure.opentelemetry;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.propagation.ContextPropagators;
import org.axonframework.messaging.Message;
import org.axonframework.messaging.correlation.CorrelationDataProvider;

import java.util.Collections;
import java.util.Map;

public class OpenTelemetryCorrelationDataProvider implements CorrelationDataProvider {
    private final ContextPropagators contextPropagators;

    public OpenTelemetryCorrelationDataProvider(ContextPropagators contextPropagators) {
        this.contextPropagators = contextPropagators;
    }

    @Override
    public Map<String, ?> correlationDataFor(Message<?> message) {
        Span currentSpan = Span.current();

        if (currentSpan.getSpanContext().isValid()) {
            MapInjector mapInjector = new MapInjector(); // will be mutated by the following line
            contextPropagators.getTextMapPropagator().inject(Context.current(), message, mapInjector);
            return mapInjector.getMetadata();
        }

        return Collections.emptyMap();
    }
}
