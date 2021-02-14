package com.example.axonsample.infrastructure.opentelemetry;

import io.opentelemetry.api.trace.Span;
import org.axonframework.common.Registration;
import org.axonframework.queryhandling.SubscriptionQueryResult;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class TraceableSubscriptionQueryResult<I, U> implements SubscriptionQueryResult<I, U> {
    private final Mono<I> initialResult;
    private final Flux<U> updates;
    private final Registration registrationDelegate;
    private final Span span;

    public TraceableSubscriptionQueryResult(SubscriptionQueryResult<I, U> subscriptionQueryResult,
                                            Span span) {
        this.initialResult = subscriptionQueryResult.initialResult();
        this.updates = subscriptionQueryResult.updates();
        this.registrationDelegate = subscriptionQueryResult;
        this.span = span;
    }

    @Override
    public Mono<I> initialResult() {
        return initialResult;
    }

    @Override
    public Flux<U> updates() {
        return updates;
    }

    @Override
    public boolean cancel() {
        span.end();
        return registrationDelegate.cancel();
    }
}
