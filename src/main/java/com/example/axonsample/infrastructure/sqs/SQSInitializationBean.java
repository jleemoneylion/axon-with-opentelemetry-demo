package com.example.axonsample.infrastructure.sqs;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.*;

import javax.annotation.PostConstruct;
import java.util.Map;

@Component
public class SQSInitializationBean {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final SqsClient sqsClient;
    private final String largeTransactionNotificationQueueName;

    public SQSInitializationBean(SqsClient sqsClient,
                                 @Value("${name.sqs-queue.large-transaction-notification}") String largeTransactionNotificationQueueName) {
        this.sqsClient = sqsClient;
        this.largeTransactionNotificationQueueName = largeTransactionNotificationQueueName;
    }

    @PostConstruct
    public void initializeQueues() {
        ListQueuesRequest listQueuesRequest = ListQueuesRequest.builder()
                .queueNamePrefix(largeTransactionNotificationQueueName)
                .maxResults(1)
                .build();
        ListQueuesResponse listQueuesResponse = sqsClient.listQueues(listQueuesRequest);

        if (listQueuesResponse.hasQueueUrls()) {
            logger.info("existing queue url = {}", listQueuesResponse.queueUrls().get(0));
        } else {
            CreateQueueRequest createQueueRequest = CreateQueueRequest.builder()
                    .queueName(largeTransactionNotificationQueueName)
                    .attributes(Map.of(QueueAttributeName.RECEIVE_MESSAGE_WAIT_TIME_SECONDS, "20"))
                    .build();
            CreateQueueResponse createQueueResponse = sqsClient.createQueue(createQueueRequest);
            logger.info("new queue url = {}", createQueueResponse.queueUrl());
        }
    }
}
