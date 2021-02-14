package com.example.axonsample.domain.account.listener.largetransaction;

import com.example.axonsample.domain.account.event.MoneyDeposited;
import com.example.axonsample.domain.account.event.MoneyWithdrawn;
import com.example.axonsample.infrastructure.objectmapper.ObjectMapperUtils;
import com.example.axonsample.infrastructure.opentelemetry.SQSOpenTelemetry;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.axonframework.eventhandling.EventHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;

import java.math.BigDecimal;

@Component
public class LargeTransactionNotificationPublisher {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final SqsClient sqsClient;
    private final String largeTransactionNotificationQueueUrl;
    private final SQSOpenTelemetry sqsOpenTelemetry;
    private final ObjectMapper objectMapper;

    public LargeTransactionNotificationPublisher(SqsClient sqsClient,
                                                 @Value("${url-prefix.sqs-queue}") String urlPrefix,
                                                 @Value("${name.sqs-queue.large-transaction-notification}") String largeTransactionNotificationQueueName,
                                                 SQSOpenTelemetry sqsOpenTelemetry) {
        this.sqsClient = sqsClient;
        this.largeTransactionNotificationQueueUrl = urlPrefix + largeTransactionNotificationQueueName;
        this.sqsOpenTelemetry = sqsOpenTelemetry;
        this.objectMapper = ObjectMapperUtils.instance();
    }

    @EventHandler
    public void on(MoneyDeposited evt) {
        if (evt.getAmount().compareTo(BigDecimal.valueOf(1000)) > 0)
            publishNotification(evt);
    }

    @EventHandler
    public void on(MoneyWithdrawn evt) {
        if (evt.getAmount().compareTo(BigDecimal.valueOf(1000)) > 0)
            publishNotification(evt);
    }

    private void publishNotification(Object evt) {
        SendMessageRequest sendMessageRequest = sqsOpenTelemetry.injectInto(SendMessageRequest.builder()
                .queueUrl(largeTransactionNotificationQueueUrl)
                .messageBody(jsonEncode(evt))
                .build());

        sqsClient.sendMessage(sendMessageRequest);
    }

    private String jsonEncode(Object object) {
        try {
            return objectMapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            logger.warn("failed to process json", e);
            throw new IllegalArgumentException("failed to process json", e);
        }
    }
}
