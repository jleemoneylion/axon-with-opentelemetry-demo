package com.example.axonsample.infrastructure.opentelemetry;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanBuilder;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Scope;
import org.axonframework.common.Registration;
import org.axonframework.messaging.GenericMessage;
import org.axonframework.messaging.MessageDispatchInterceptor;
import org.axonframework.messaging.responsetypes.ResponseType;
import org.axonframework.queryhandling.*;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Stream;

public class OpenTelemetryQueryGateway implements QueryGateway {
    private final Tracer tracer;
    private final QueryGateway delegate;

    public OpenTelemetryQueryGateway(Tracer tracer,
                                     QueryBus queryBus) {
        this.tracer = tracer;
        this.delegate = DefaultQueryGateway.builder()
                .queryBus(queryBus)
                .build();
    }

    @Override
    public <R, Q> CompletableFuture<R> query(String queryName,
                                             Q query,
                                             ResponseType<R> responseType) {
        QueryMessage<?, R> queryMessage = new GenericQueryMessage<>(GenericMessage.asMessage(query), queryName, responseType);
        return getWithSpan("query_" + OpenTelemetryUtils.messageName(queryMessage.getClass(), queryName),
                queryMessage,
                span -> delegate.query(queryName, queryMessage, responseType)
                        .whenComplete((r, e) -> span.end()));
    }

    @Override
    public <R, Q> Stream<R> scatterGather(String queryName,
                                          Q query,
                                          ResponseType<R> responseType,
                                          long timeout,
                                          TimeUnit timeUnit) {
        QueryMessage<?, R> queryMessage = new GenericQueryMessage<>(GenericMessage.asMessage(query), queryName, responseType);
        return getWithSpan("scatterGather_" + OpenTelemetryUtils.messageName(queryMessage.getClass(), queryName),
                queryMessage,
                span -> delegate.scatterGather(queryName, queryMessage, responseType, timeout, timeUnit)
                        .onClose(span::end));
    }

    @Override
    public <Q, I, U> SubscriptionQueryResult<I, U> subscriptionQuery(String queryName,
                                                                     Q query,
                                                                     ResponseType<I> initialResponseType,
                                                                     ResponseType<U> updateResponseType,
                                                                     SubscriptionQueryBackpressure backpressure,
                                                                     int updateBufferSize) {
        SubscriptionQueryMessage<?, I, U> queryMessage = new GenericSubscriptionQueryMessage<>(
                GenericMessage.asMessage(query), queryName, initialResponseType, updateResponseType);

        return getWithSpan("subscriptionQuery_" + OpenTelemetryUtils.messageName(query.getClass(), queryName),
                queryMessage,
                span -> {
                    SubscriptionQueryResult<I, U> subscriptionQueryResult = delegate.subscriptionQuery(
                            queryName, queryMessage, initialResponseType, updateResponseType, backpressure,
                            updateBufferSize
                    );
                    return new TraceableSubscriptionQueryResult<>(subscriptionQueryResult, span);
                });
    }

    @Override
    public Registration registerDispatchInterceptor(MessageDispatchInterceptor<? super QueryMessage<?, ?>> dispatchInterceptor) {
        return delegate.registerDispatchInterceptor(dispatchInterceptor);
    }

    private <R, T> T getWithSpan(String operation, QueryMessage<?, R> queryMessage, Function<Span, T> spanSupplier) {
        SpanBuilder spanBuilder = tracer.spanBuilder(operation)
                .setSpanKind(Span.Kind.CLIENT);
        OpenTelemetryUtils.addMessageAttributes(spanBuilder, queryMessage);
        Span span = spanBuilder.startSpan();

        try (Scope ignored = span.makeCurrent()) {
            return spanSupplier.apply(span);
        }
    }
}
