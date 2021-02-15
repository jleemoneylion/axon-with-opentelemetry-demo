package com.example.axonsample.infrastructure.opentelemetry;

import com.example.axonsample.infrastructure.objectmapper.ObjectMapperUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanBuilder;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.Scope;
import io.opentelemetry.context.propagation.ContextPropagators;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.services.sqs.model.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class SQSOpenTelemetry {
    private static final Logger logger = LoggerFactory.getLogger(SQSOpenTelemetry.class);

    private static final String SPAN_CONTEXT_ATTRIBUTE_KEY = "span_ctx";
    private static final String STRING_DATA_TYPE = "String";
    private static final ObjectMapper objectMapper = ObjectMapperUtils.instance();

    private final Tracer tracer;
    private final ContextPropagators contextPropagators;
    private final String attributeKey;

    public SQSOpenTelemetry(Tracer tracer,
                            ContextPropagators contextPropagators,
                            String attributeKey) {
        this.tracer = tracer;
        this.contextPropagators = contextPropagators;
        this.attributeKey = attributeKey;
    }

    public SQSOpenTelemetry(Tracer tracer, ContextPropagators contextPropagators) {
        this(tracer, contextPropagators, SPAN_CONTEXT_ATTRIBUTE_KEY);
    }

    public SendMessageRequest injectInto(SendMessageRequest request) {
        Span currentSpan = Span.current();

        if (currentSpan.getSpanContext().isValid()) {
            return request.toBuilder()
                    .messageAttributes(tracedAttributes(request))
                    .build();
        }

        return request;
    }

    public SendMessageBatchRequestEntry injectInto(SendMessageBatchRequestEntry request) {
        Span currentSpan = Span.current();

        if (currentSpan.getSpanContext().isValid()) {
            return request.toBuilder()
                    .messageAttributes(tracedAttributes(request))
                    .build();
        }

        return request;
    }

    public SendMessageBatchRequest injectInto(SendMessageBatchRequest request) {
        Span currentSpan = Span.current();

        if (currentSpan.getSpanContext().isValid()) {
            return request.toBuilder()
                    .entries(request.entries().stream()
                            .map(this::injectInto)
                            .collect(Collectors.toList()))
                    .build();
        }

        return request;
    }

    public Map<String, Context> extractFrom(ReceiveMessageResponse response) {
        return flattenMap(response.messages().stream()
                .collect(Collectors.toMap(Message::messageId, message -> {
                    if (message.messageAttributes().containsKey(attributeKey)) {
                        MessageAttributeValue messageAttributeValue = message.messageAttributes().get(attributeKey);
                        Map<String, String> metadata = jsonDecode(messageAttributeValue.stringValue());
                        SQSMessageMetadataExtractor metadataExtractor = new SQSMessageMetadataExtractor(metadata);
                        Context extractedContext = contextPropagators.getTextMapPropagator().extract(Context.current(), message, metadataExtractor);
                        return Optional.of(extractedContext);
                    }

                    return Optional.empty();
                })));
    }

    public void consumeWithContext(Context context,
                                   String queueName,
                                   Runnable runnable) {
        if (context == null) {
            runnable.run();
            return;
        }

        Span span = null;

        try (Scope ignored = context.makeCurrent()) {
            SpanBuilder spanBuilder = tracer.spanBuilder(queueName + " receive")
                    .setSpanKind(Span.Kind.CONSUMER);
            span = spanBuilder.startSpan();
            runnable.run();
        } finally {
            if (span != null)
                span.end();
        }
    }

    private <T> Map<String, T> flattenMap(Map<String, Optional<T>> mapWithOptionals) {
        final Map<String, T> flattened = new HashMap<>();
        mapWithOptionals.forEach((k, v) -> v.ifPresent(t -> flattened.put(k, t)));
        return flattened;
    }

    private <T> Map<String, MessageAttributeValue> tracedAttributes(T request) {
        MapInjector mapInjector = new MapInjector(); // will be mutated by the following line
        contextPropagators.getTextMapPropagator().inject(Context.current(), request, mapInjector);
        Map<String, String> metadata = mapInjector.getMetadata();
        MessageAttributeValue messageAttributeValue = MessageAttributeValue.builder()
                .dataType(STRING_DATA_TYPE)
                .stringValue(jsonEncode(metadata))
                .build();
        Map<String, MessageAttributeValue> attributes = new HashMap<>();
        attributes.put(attributeKey, messageAttributeValue);
        return attributes;
    }

    private String jsonEncode(Map<String, String> metadata) {
        try {
            return objectMapper.writeValueAsString(metadata);
        } catch (JsonProcessingException e) {
            logger.warn("failed to process json", e);
        }
        return null;
    }

    private Map<String, String> jsonDecode(String metadataString) {
        try {
            return objectMapper.readValue(metadataString, new TypeReference<Map<String, String>>() {
            });
        } catch (JsonProcessingException e) {
            logger.warn("failed to process json", e);
        }
        return Collections.emptyMap();
    }
}
