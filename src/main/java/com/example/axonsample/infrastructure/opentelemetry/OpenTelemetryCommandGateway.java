package com.example.axonsample.infrastructure.opentelemetry;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanBuilder;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Scope;
import org.axonframework.commandhandling.*;
import org.axonframework.commandhandling.callbacks.FailureLoggingCallback;
import org.axonframework.commandhandling.callbacks.FutureCallback;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.commandhandling.gateway.DefaultCommandGateway;
import org.axonframework.common.Registration;
import org.axonframework.messaging.MessageDispatchInterceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Function;

public class OpenTelemetryCommandGateway implements CommandGateway {
    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final Tracer tracer;
    private final CommandGateway delegate;

    public OpenTelemetryCommandGateway(Tracer tracer,
                                       CommandBus commandBus) {
        this.tracer = tracer;
        this.delegate = DefaultCommandGateway.builder()
                .commandBus(commandBus)
                .build();
    }

    @Override
    public <C, R> void send(C command, CommandCallback<? super C, ? super R> callback) {
        CommandMessage<? super C> cmd = GenericCommandMessage.asCommandMessage(command);
        sendWithSpan("send_" + OpenTelemetryUtils.messageName(cmd), cmd, span -> {
            CompletableFuture<?> resultReceived = new CompletableFuture<>();
            delegate.send(cmd, (CommandCallback<Object, R>) (commandMessage, commandResultMessage) -> {
                try (Scope ignored = span.makeCurrent()) {
                    //noinspection unchecked
                    callback.onResult((CommandMessage<? extends C>) commandMessage, commandResultMessage);
                } finally {
                    resultReceived.complete(null);
                }
            });
            resultReceived.thenRun(span::end);
        });
    }

    @Override
    public <R> R sendAndWait(Object command) {
        return doSendAndExtract(command, FutureCallback::getResult);
    }

    @Override
    public <R> R sendAndWait(Object command, long timeout, TimeUnit unit) {
        return doSendAndExtract(command, FutureCallback::getResult);
    }

    @Override
    public <R> CompletableFuture<R> send(Object command) {
        FutureCallback<Object, R> callback = new FutureCallback<>();
        send(command, new FailureLoggingCallback<>(logger, callback));
        CompletableFuture<R> result = new CompletableFuture<>();
        callback.exceptionally(GenericCommandResultMessage::asCommandResultMessage)
                .thenAccept(r -> {
                    try {
                        if (r.isExceptional()) {
                            result.completeExceptionally(r.exceptionResult());
                        } else {
                            result.complete(r.getPayload());
                        }
                    } catch (Exception e) {
                        result.completeExceptionally(e);
                    }
                });
        return result;
    }

    @Override
    public Registration registerDispatchInterceptor(MessageDispatchInterceptor<? super CommandMessage<?>> dispatchInterceptor) {
        return null;
    }

    private <R> R doSendAndExtract(Object command,
                                   Function<FutureCallback<Object, R>, CommandResultMessage<? extends R>> resultExtractor) {
        FutureCallback<Object, R> futureCallback = new FutureCallback<>();

        CommandMessage<?> cmd = GenericCommandMessage.asCommandMessage(command);
        sendWithSpan("sendAndWait_" + OpenTelemetryUtils.messageName(cmd), cmd, span -> {
            delegate.send(cmd, futureCallback);
            futureCallback.thenRun(span::end);
        });
        CommandResultMessage<? extends R> commandResultMessage = resultExtractor.apply(futureCallback);
        if (commandResultMessage.isExceptional()) {
            throw asRuntime(commandResultMessage.exceptionResult());
        }
        return commandResultMessage.getPayload();
    }

    private void sendWithSpan(String operation, CommandMessage<?> commandMessage, Consumer<Span> spanConsumer) {
        SpanBuilder spanBuilder = tracer.spanBuilder(operation)
                .setSpanKind(Span.Kind.CLIENT);
        OpenTelemetryUtils.addMessageAttributes(spanBuilder, commandMessage);
        Span span = spanBuilder.startSpan();

        try (Scope ignored = span.makeCurrent()) {
            spanConsumer.accept(span);
        }
    }

    private RuntimeException asRuntime(Throwable e) {
        if (e instanceof Error) {
            throw (Error) e;
        } else if (e instanceof RuntimeException) {
            return (RuntimeException) e;
        } else {
            return new CommandExecutionException("An exception occurred while executing a command", e);
        }
    }
}
