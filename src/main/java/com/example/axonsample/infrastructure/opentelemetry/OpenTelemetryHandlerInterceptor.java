package com.example.axonsample.infrastructure.opentelemetry;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanBuilder;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.Scope;
import io.opentelemetry.context.propagation.ContextPropagators;
import org.axonframework.messaging.InterceptorChain;
import org.axonframework.messaging.Message;
import org.axonframework.messaging.MessageHandlerInterceptor;
import org.axonframework.messaging.unitofwork.UnitOfWork;

public class OpenTelemetryHandlerInterceptor implements MessageHandlerInterceptor<Message<?>> {
    private final Tracer tracer;
    private final ContextPropagators contextPropagators;

    public OpenTelemetryHandlerInterceptor(Tracer tracer,
                                           ContextPropagators contextPropagators) {
        this.tracer = tracer;
        this.contextPropagators = contextPropagators;
    }

    @Override
    public Object handle(UnitOfWork<? extends Message<?>> unitOfWork, InterceptorChain interceptorChain) throws Exception {
        Message<?> message = unitOfWork.getMessage();
        String operationName = "handle_" + OpenTelemetryUtils.messageName(message);

        Context extractedContext = contextPropagators.getTextMapPropagator().extract(Context.current(), message, new AxonMessageMapExtractor());

        try (Scope ignored = extractedContext.makeCurrent()) {
            SpanBuilder spanBuilder = tracer.spanBuilder(operationName)
                    .setSpanKind(Span.Kind.SERVER);
            OpenTelemetryUtils.addMessageAttributes(spanBuilder, message);
            Span span = spanBuilder.startSpan();
            unitOfWork.onCleanup(u -> span.end());
            return interceptorChain.proceed();
        }
    }
}
