package com.example.axonsample.infrastructure.opentelemetry;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.propagation.ContextPropagators;
import org.axonframework.messaging.Message;
import org.axonframework.messaging.MessageDispatchInterceptor;

import java.util.List;
import java.util.function.BiFunction;

public class OpenTelemetryDispatchInterceptor implements MessageDispatchInterceptor<Message<?>> {
    private final ContextPropagators contextPropagators;

    public OpenTelemetryDispatchInterceptor(ContextPropagators contextPropagators) {
        this.contextPropagators = contextPropagators;
    }

    @Override
    public BiFunction<Integer, Message<?>, Message<?>> handle(List<? extends Message<?>> messages) {
        Span currentSpan = Span.current();

        if (currentSpan.getSpanContext().isValid()) {
            return (index, message) -> {
                MapInjector mapInjector = new MapInjector(); // will be mutated by the following line
                contextPropagators.getTextMapPropagator().inject(Context.current(), message, mapInjector);
                return message.andMetaData(mapInjector.getMetadata());
            };
        } else {
            return (index, message) ->
                    message;
        }
    }
}
