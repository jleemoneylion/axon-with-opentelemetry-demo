package com.example.axonsample.infrastructure.opentelemetry;

import io.opentelemetry.api.trace.SpanBuilder;
import org.axonframework.commandhandling.CommandMessage;
import org.axonframework.eventhandling.DomainEventMessage;
import org.axonframework.eventhandling.EventMessage;
import org.axonframework.messaging.Message;
import org.axonframework.queryhandling.QueryMessage;

public class OpenTelemetryUtils {
    public static String messageName(Message<?> message) {
        if (message instanceof CommandMessage)
            return messageName(message.getPayloadType(), ((CommandMessage<?>) message).getCommandName());
        else if (message instanceof QueryMessage)
            return messageName(message.getPayloadType(), ((QueryMessage<?, ?>) message).getQueryName());
        else
            return message.getPayloadType().getSimpleName();
    }

    public static String messageName(Class<?> payloadType, String name) {
        if (!payloadType.getName().equalsIgnoreCase(name))
            return name;
        return payloadType.getSimpleName();
    }

    public static void addMessageAttributes(SpanBuilder spanBuilder, Message<?> message) {
        if (message instanceof CommandMessage)
            addCommandMessageAttributes(spanBuilder, (CommandMessage<?>) message);
        else if (message instanceof EventMessage)
            addEventMessageAttributes(spanBuilder, (EventMessage<?>) message);
        else if (message instanceof QueryMessage) {
            addQueryMessageAttributes(spanBuilder, (QueryMessage<?, ?>) message);
        }
    }

    private static void addCommandMessageAttributes(SpanBuilder spanBuilder, CommandMessage<?> commandMessage) {
        String messageId = commandMessage.getIdentifier();
        String messageType = commandMessage.getClass().getSimpleName();
        String payloadType = commandMessage.getPayloadType().getName();
        String messageName = OpenTelemetryUtils.messageName(commandMessage);

        spanBuilder.setAttribute(MessageTag.MESSAGE_ID.getTagKey(), messageId);
        spanBuilder.setAttribute(MessageTag.MESSAGE_TYPE.getTagKey(), messageType);
        spanBuilder.setAttribute(MessageTag.PAYLOAD_TYPE.getTagKey(), payloadType);
        spanBuilder.setAttribute(MessageTag.MESSAGE_NAME.getTagKey(), messageName);
    }

    private static void addEventMessageAttributes(SpanBuilder spanBuilder, EventMessage<?> eventMessage) {
        String messageId = eventMessage.getIdentifier();
        String aggregateId = eventMessage instanceof DomainEventMessage ? ((DomainEventMessage<?>) eventMessage).getAggregateIdentifier() : null;
        String messageType = eventMessage.getClass().getSimpleName();
        String payloadType = eventMessage.getPayloadType().getName();

        spanBuilder.setAttribute(MessageTag.MESSAGE_ID.getTagKey(), messageId);
        spanBuilder.setAttribute(MessageTag.MESSAGE_TYPE.getTagKey(), messageType);
        spanBuilder.setAttribute(MessageTag.PAYLOAD_TYPE.getTagKey(), payloadType);

        if (aggregateId != null)
            spanBuilder.setAttribute(MessageTag.AGGREGATE_ID.getTagKey(), aggregateId);
    }

    private static void addQueryMessageAttributes(SpanBuilder spanBuilder, QueryMessage<?, ?> queryMessage) {
        String messageId = queryMessage.getIdentifier();
        String messageType = queryMessage.getClass().getSimpleName();
        String payloadType = queryMessage.getPayloadType().getName();
        String messageName = OpenTelemetryUtils.messageName(queryMessage);

        spanBuilder.setAttribute(MessageTag.MESSAGE_ID.getTagKey(), messageId);
        spanBuilder.setAttribute(MessageTag.MESSAGE_TYPE.getTagKey(), messageType);
        spanBuilder.setAttribute(MessageTag.PAYLOAD_TYPE.getTagKey(), payloadType);
        spanBuilder.setAttribute(MessageTag.MESSAGE_NAME.getTagKey(), messageName);
    }
}

