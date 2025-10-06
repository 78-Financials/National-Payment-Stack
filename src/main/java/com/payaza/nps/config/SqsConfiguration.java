package com.payaza.nps.config;

import io.awspring.cloud.sqs.config.SqsMessageListenerContainerFactory;
import io.awspring.cloud.sqs.listener.acknowledgement.AcknowledgementMode;
import io.awspring.cloud.sqs.listener.errorhandler.AsyncErrorHandler;
import io.awspring.cloud.sqs.operations.SqsTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sqs.SqsAsyncClient;

/**
 * SQS Configuration for Amazon SQS integration
 */
@Configuration
public class SqsConfiguration {

    private static final Logger logger = LoggerFactory.getLogger(SqsConfiguration.class);

    @Value("${aws.region:us-east-1}")
    private String awsRegion;

    @Value("${aws.sqs.inbound-pacs008-queue:inbound-pacs008-queue}")
    private String inboundPacs008Queue;

    @Value("${aws.sqs.inbound-error-queue:inbound-error-queue}")
    private String inboundErrorQueue;

    @Value("${aws.sqs.outbound-pacs002-queue:outbound-pacs002-queue}")
    private String outboundPacs002Queue;

    /**
     * Configure SQS Async Client
     */
    @Bean
    public SqsAsyncClient sqsAsyncClient() {
        logger.info("Configuring SQS Async Client for region: {}", awsRegion);
        
        return SqsAsyncClient.builder()
            .region(Region.of(awsRegion))
            .credentialsProvider(DefaultCredentialsProvider.create())
            .build();
    }

    /**
     * Configure SQS Template for sending messages
     */
    @Bean
    public SqsTemplate sqsTemplate(SqsAsyncClient sqsAsyncClient) {
        logger.info("Configuring SQS Template");
        
        return SqsTemplate.builder()
            .sqsAsyncClient(sqsAsyncClient)
            .build();
    }

    /**
     * Configure SQS Message Listener Container Factory
     */
    @Bean
    public SqsMessageListenerContainerFactory<Object> defaultSqsListenerContainerFactory(
            SqsAsyncClient sqsAsyncClient) {
        logger.info("Configuring SQS Message Listener Container Factory");
        
        return SqsMessageListenerContainerFactory.builder()
            .sqsAsyncClient(sqsAsyncClient)
            .acknowledgementMode(AcknowledgementMode.ON_SUCCESS)
            .build();
    }

    /**
     * Configure error handler for SQS messages
     */
    @Bean
    public AsyncErrorHandler<Object> sqsErrorHandler() {
        logger.info("Configuring SQS Error Handler");
        
        return (message, exception) -> {
            logger.error("Error processing SQS message: {}", exception.getMessage(), exception);
            // Additional error handling logic can be added here
        };
    }

    /**
     * Get inbound PACS.008 queue name
     */
    public String getInboundPacs008Queue() {
        return inboundPacs008Queue;
    }

    /**
     * Get inbound error queue name
     */
    public String getInboundErrorQueue() {
        return inboundErrorQueue;
    }

    /**
     * Get outbound PACS.002 queue name
     */
    public String getOutboundPacs002Queue() {
        return outboundPacs002Queue;
    }
}
