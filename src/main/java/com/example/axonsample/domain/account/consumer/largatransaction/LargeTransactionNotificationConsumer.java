package com.example.axonsample.domain.account.consumer.largatransaction;

import com.example.axonsample.domain.account.dto.LargeTransactionNotification;
import com.example.axonsample.infrastructure.objectmapper.ObjectMapperUtils;
import com.example.axonsample.infrastructure.opentelemetry.SQSOpenTelemetry;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.Scope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.DeleteMessageRequest;
import software.amazon.awssdk.services.sqs.model.Message;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageRequest;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageResponse;

import java.util.List;
import java.util.Map;

@Component
public class LargeTransactionNotificationConsumer {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final SqsClient sqsClient;
    private final String largeTransactionNotificationQueueName;
    private final String largeTransactionNotificationQueueUrl;
    private final SQSOpenTelemetry sqsOpenTelemetry;
    private final ObjectMapper objectMapper;

    public LargeTransactionNotificationConsumer(SqsClient sqsClient,
                                                @Value("${url-prefix.sqs-queue}") String urlPrefix,
                                                @Value("${name.sqs-queue.large-transaction-notification}") String largeTransactionNotificationQueueName,
                                                SQSOpenTelemetry sqsOpenTelemetry) {
        this.sqsClient = sqsClient;
        this.largeTransactionNotificationQueueName = largeTransactionNotificationQueueName;
        this.sqsOpenTelemetry = sqsOpenTelemetry;
        this.largeTransactionNotificationQueueUrl = urlPrefix + largeTransactionNotificationQueueName;
        this.objectMapper = ObjectMapperUtils.instance();
    }

    @Scheduled(fixedDelay = 1L)
    public void execute() {
        try {
            pollAndProcessMessages();
            logger.info("polled and processed messages");
        } catch (Exception e) {
            logger.warn("failed to poll and process messages", e);
        }
    }

    private void pollAndProcessMessages() {
        ReceiveMessageRequest receiveMessageRequest = ReceiveMessageRequest.builder()
                .queueUrl(largeTransactionNotificationQueueUrl)
                .maxNumberOfMessages(10)
                .messageAttributeNames("All")
                .build();
        ReceiveMessageResponse receiveMessageResponse = sqsClient.receiveMessage(receiveMessageRequest);

        Map<String, Context> messageIdsToExtractedContexts = sqsOpenTelemetry.extractFrom(receiveMessageResponse);
        List<Message> messages = receiveMessageResponse.messages();

        for (Message message : messages) {
            LargeTransactionNotification notification = jsonDecode(message.body());

            Context extractedContext = messageIdsToExtractedContexts.get(message.messageId());
            sqsOpenTelemetry.consumeWithContext(
                    extractedContext,
                    largeTransactionNotificationQueueName,
                    () -> handleNotification(notification, message));
        }
    }

    private void handleNotification(LargeTransactionNotification notification,
                                    Message message) {
        logger.info("handle large transaction notification: " +
                        "account id = {}, " +
                        "transaction id = {}, " +
                        "amount = {}, " +
                        "timestamp = {}",
                notification.getAccountId(),
                notification.getTransactionId(),
                notification.getAmount(),
                notification.getTimestamp());
        sqsClient.deleteMessage(DeleteMessageRequest.builder()
                .queueUrl(largeTransactionNotificationQueueUrl)
                .receiptHandle(message.receiptHandle())
                .build());
    }

    private LargeTransactionNotification jsonDecode(String s) {
        try {
            return objectMapper.readValue(s, LargeTransactionNotification.class);
        } catch (JsonProcessingException e) {
            logger.warn("failed to process json", e);
            throw new IllegalArgumentException("failed to process json", e);
        }
    }
}
