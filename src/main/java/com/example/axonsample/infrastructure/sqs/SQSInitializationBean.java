package com.example.axonsample.infrastructure.sqs;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.CreateQueueRequest;
import software.amazon.awssdk.services.sqs.model.CreateQueueResponse;
import software.amazon.awssdk.services.sqs.model.ListQueuesRequest;
import software.amazon.awssdk.services.sqs.model.ListQueuesResponse;

import javax.annotation.PostConstruct;

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
                    .build();
            CreateQueueResponse createQueueResponse = sqsClient.createQueue(createQueueRequest);
            logger.info("new queue url = {}", createQueueResponse.queueUrl());
        }
    }
}
