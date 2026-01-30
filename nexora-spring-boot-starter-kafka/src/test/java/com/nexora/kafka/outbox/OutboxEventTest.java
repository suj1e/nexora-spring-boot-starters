package com.nexora.kafka.outbox;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link OutboxEvent}.
 */
@DisplayName("OutboxEvent Tests")
class OutboxEventTest {

    @Test
    @DisplayName("Factory method should create event with correct fields")
    void testFactoryMethodCreatesEvent() {
        String eventType = "USER_CREATED";
        String topic = "user-events";
        String bizId = "user123";
        String payload = "{\"userId\":123,\"username\":\"test\"}";

        OutboxEvent event = OutboxEvent.of(eventType, topic, bizId, payload);

        assertAll("OutboxEvent created by factory",
            () -> assertEquals(eventType, event.getEventType()),
            () -> assertEquals(topic, event.getTopic()),
            () -> assertEquals(bizId, event.getBizId()),
            () -> assertEquals(payload, event.getPayload()),
            () -> assertEquals(OutboxStatus.NEW, event.getStatus()),
            () -> assertEquals(0, event.getRetryCount())
        );
    }

    @Test
    @DisplayName("MarkAsSent should update status to SENT")
    void testMarkAsSent() {
        OutboxEvent event = OutboxEvent.of("EVENT", "topic", "biz123", "{}");
        event.setStatus(OutboxStatus.NEW);

        event.markAsSent();

        assertEquals(OutboxStatus.SENT, event.getStatus());
    }

    @Test
    @DisplayName("MarkAsFailed should update status to FAILED")
    void testMarkAsFailed() {
        OutboxEvent event = OutboxEvent.of("EVENT", "topic", "biz123", "{}");
        event.setStatus(OutboxStatus.NEW);

        event.markAsFailed();

        assertEquals(OutboxStatus.FAILED, event.getStatus());
    }

    @Test
    @DisplayName("IncrementRetry should increment retry count")
    void testIncrementRetry() {
        OutboxEvent event = OutboxEvent.of("EVENT", "topic", "biz123", "{}");
        event.setRetryCount(2);

        event.incrementRetry();

        assertEquals(3, event.getRetryCount());
    }

    @Test
    @DisplayName("Multiple incrementRetry calls should accumulate")
    void testMultipleIncrementRetries() {
        OutboxEvent event = OutboxEvent.of("EVENT", "topic", "biz123", "{}");

        event.incrementRetry();
        event.incrementRetry();
        event.incrementRetry();

        assertEquals(3, event.getRetryCount());
    }

    @Test
    @DisplayName("Default status should be NEW")
    void testDefaultStatus() {
        OutboxEvent event = new OutboxEvent();

        assertEquals(OutboxStatus.NEW, event.getStatus());
    }

    @Test
    @DisplayName("Default retry count should be 0")
    void testDefaultRetryCount() {
        OutboxEvent event = new OutboxEvent();

        assertEquals(0, event.getRetryCount());
    }

    @Test
    @DisplayName("All fields should be settable")
    void testAllFieldsSettable() {
        OutboxEvent event = new OutboxEvent();
        Instant now = Instant.now();

        event.setId(999L);
        event.setEventType("TEST_EVENT");
        event.setTopic("test-topic");
        event.setBizId("test-biz-id");
        event.setPayload("{\"test\":\"data\"}");
        event.setStatus(OutboxStatus.SENT);
        event.setRetryCount(5);
        event.setCreatedAt(now);
        event.setUpdatedAt(now);

        assertAll("All fields set correctly",
            () -> assertEquals(999L, event.getId()),
            () -> assertEquals("TEST_EVENT", event.getEventType()),
            () -> assertEquals("test-topic", event.getTopic()),
            () -> assertEquals("test-biz-id", event.getBizId()),
            () -> assertEquals("{\"test\":\"data\"}", event.getPayload()),
            () -> assertEquals(OutboxStatus.SENT, event.getStatus()),
            () -> assertEquals(5, event.getRetryCount()),
            () -> assertEquals(now, event.getCreatedAt()),
            () -> assertEquals(now, event.getUpdatedAt())
        );
    }
}
