package com.nexora.kafka.publisher;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

/**
 * Event publisher service using Outbox Pattern.
 *
 * <p>When Outbox is enabled, events are written to the outbox table within the same transaction
 * as the business logic. The events are then published to Kafka by a background job.
 *
 * <p>Usage:
 * <pre>
 * &#64;Autowired
 * private EventPublisher eventPublisher;
 *
 * eventPublisher.publish("USER_CREATED", userId, username, email, name, metadata);
 * </pre>
 *
 * @author sujie
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class EventPublisher {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    /**
     * Publish an event to Kafka.
     *
     * <p>If Outbox is enabled, the event will be written to the outbox table instead.
     *
     * @param eventType the event type
     * @param bizId     the business ID
     * @param topic     the Kafka topic
     * @param payload   the event payload (will be serialized to JSON)
     */
    @Transactional
    public void publish(String eventType, String bizId, String topic, Map<String, Object> payload) {
        try {
            String payloadJson = objectMapper.writeValueAsString(payload);
            kafkaTemplate.send(topic, bizId, payloadJson);
            log.debug("Published event: type={}, bizId={}, topic={}", eventType, bizId, topic);
        } catch (Exception e) {
            log.error("Failed to publish event: type={}, bizId={}, topic={}", eventType, bizId, topic, e);
            throw new RuntimeException("Failed to publish event", e);
        }
    }

    /**
     * Publish a user event with common fields.
     *
     * @param eventType the event type
     * @param bizId     the business ID
     * @param topic     the Kafka topic
     * @param username  the username
     * @param email     the email
     * @param name      the display name
     * @param metadata  additional metadata
     */
    @Transactional
    public void publishUserEvent(String eventType, Long bizId, String topic, String username, String email, String name, Map<String, Object> metadata) {
        Map<String, Object> payload = new java.util.HashMap<>();
        payload.put("userId", bizId);
        payload.put("username", username);
        payload.put("email", email);
        payload.put("name", name);
        if (metadata != null && !metadata.isEmpty()) {
            payload.putAll(metadata);
        }
        publish(eventType, String.valueOf(bizId), topic, payload);
    }
}
