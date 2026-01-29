package com.nexora.kafka.outbox;

/**
 * Outbox event status.
 *
 * @author sujie
 */
public enum OutboxStatus {
    /**
     * New event - not yet published.
     */
    NEW,

    /**
     * Successfully published to Kafka.
     */
    SENT,

    /**
     * Failed to publish after retries.
     */
    FAILED
}
