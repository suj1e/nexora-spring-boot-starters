package com.nexora.kafka.outbox;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;

/**
 * Outbox event entity for reliable event publishing.
 *
 * <p>This table stores events that need to be published to Kafka.
 * A background job publishes events and marks them as SENT.
 *
 * <p>Used for implementing the Outbox Pattern for reliable event publishing.
 *
 * @author sujie
 */
@Entity
@Table(name = "outbox_event")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
public class OutboxEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 64)
    private String eventType;

    @Column(nullable = false, length = 128)
    private String topic;

    @Column(nullable = false, length = 64)
    private String bizId;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String payload;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OutboxStatus status = OutboxStatus.NEW;

    @Column
    private Integer retryCount = 0;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private Instant updatedAt;

    /**
     * Create a new outbox event.
     *
     * @param eventType the event type
     * @param topic      the Kafka topic
     * @param bizId      the business ID
     * @param payload    the event payload (JSON)
     * @return the outbox event
     */
    public static OutboxEvent of(String eventType, String topic, String bizId, String payload) {
        OutboxEvent event = new OutboxEvent();
        event.setEventType(eventType);
        event.setTopic(topic);
        event.setBizId(bizId);
        event.setPayload(payload);
        return event;
    }

    /**
     * Mark as sent.
     */
    public void markAsSent() {
        this.status = OutboxStatus.SENT;
    }

    /**
     * Mark as failed.
     */
    public void markAsFailed() {
        this.status = OutboxStatus.FAILED;
    }

    /**
     * Increment retry count.
     */
    public void incrementRetry() {
        this.retryCount++;
    }
}
