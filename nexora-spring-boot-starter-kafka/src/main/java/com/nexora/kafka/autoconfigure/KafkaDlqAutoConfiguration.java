package com.nexora.kafka.autoconfigure;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.CommonErrorHandler;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.FixedBackOff;

/**
 * Kafka DLQ (Dead Letter Queue) auto-configuration.
 *
 * <p>When message consumption fails, the message is sent to a DLQ topic
 * for later processing or manual intervention.
 *
 * <p>DLQ topic naming: {original-topic}.dlq
 *
 * @author sujie
 */
@Slf4j
@Configuration
@ConditionalOnClass(name = "org.springframework.kafka.core.KafkaTemplate")
@ConditionalOnProperty(prefix = "nexora.kafka.dlq", name = "enabled", havingValue = "true", matchIfMissing = true)
public class KafkaDlqAutoConfiguration {

    @Autowired(required = false)
    private KafkaTemplate<Object, Object> kafkaTemplate;

    /**
     * Common error handler with DLQ support.
     *
     * <p>Configuration:
     * <ul>
     *   <li>Max retry attempts: 3</li>
     * <li>Backoff interval: 1 second</li>
   *   <li>Failed messages sent to DLQ topic</li>
     * </ul>
     */
    @Bean
    public CommonErrorHandler kafkaErrorHandler() {
        if (kafkaTemplate == null) {
            log.warn("KafkaTemplate not available, DLQ disabled");
            // Return a simple error handler without DLQ
            return new DefaultErrorHandler();
        }

        // Fixed backoff: retry after 1 second, max 3 attempts
        FixedBackOff backOff = new FixedBackOff(1000L, 3L);

        // Dead letter publishing recoverer
        // Automatically sends failed messages to DLQ topic
        DeadLetterPublishingRecoverer recoverer = new DeadLetterPublishingRecoverer(
            kafkaTemplate,
            (record, exception) -> {
                // DLQ topic name: original-topic + ".dlq"
                String dlqTopic = record.topic() + ".dlq";
                return new org.apache.kafka.common.TopicPartition(dlqTopic, record.partition());
            }
        );

        // Default error handler with DLQ
        DefaultErrorHandler errorHandler = new DefaultErrorHandler(
            recoverer,
            backOff
        );

        // Configure which exceptions should NOT be retried
        errorHandler.addNotRetryableExceptions(
            IllegalArgumentException.class,
            org.springframework.kafka.support.serializer.DeserializationException.class
        );

        return errorHandler;
    }
}
