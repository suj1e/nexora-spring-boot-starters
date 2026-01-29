package com.nexora.kafka.autoconfigure;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.kafka.annotation.EnableKafka;

/**
 * Kafka auto-configuration.
 *
 * <p>Automatically configures:
 * <ul>
 *   <li>Kafka template for event publishing</li>
 *   <li>DLQ (Dead Letter Queue) error handler</li>
 *   <li>Outbox pattern support (if enabled)</li>
 * </ul>
 *
 * <p>Configuration properties (application.yml):
 * <pre>
 * nexora:
 *   kafka:
 *     dlq:
 *       enabled: true
 *       retry-attempts: 3
 *     outbox:
 *       enabled: true
 * </pre>
 *
 * @author sujie
 */
@AutoConfiguration
@ConditionalOnClass(org.springframework.kafka.core.KafkaTemplate.class)
@EnableKafka
@ComponentScan(basePackageClasses = com.nexora.kafka.publisher.EventPublisher.class)
public class KafkaAutoConfiguration {

    /**
     * Outbox pattern support configuration.
     * Only active when JPA is available and outbox is enabled.
     */
    @ConditionalOnClass(name = "org.springframework.boot.autoconfigure.domain.EntityScan")
    @ConditionalOnProperty(prefix = "nexora.kafka.outbox", name = "enabled", havingValue = "true", matchIfMissing = false)
    public static class OutboxConfiguration {
        // Outbox entity scanning will be handled by Spring Boot auto-configuration
        // when spring-boot-starter-data-jpa is on the classpath
    }
}
