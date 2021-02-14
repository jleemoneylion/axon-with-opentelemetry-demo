package com.example.axonsample.infrastructure.sqs;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.SqsClientBuilder;

import java.net.URI;

@Configuration
public class SQSConfig {
    @Bean
    public SqsClient sqsClient(@Value("${url.aws-override:}") String endpointOverride) {
        SqsClientBuilder builder = SqsClient.builder()
                .region(Region.US_EAST_1);

        System.out.println("ENDPOINT OVERRIDE =" + endpointOverride);

        if (!StringUtils.isEmpty(endpointOverride))
            builder = builder.endpointOverride(URI.create(endpointOverride));

        return builder.build();
    }
}
