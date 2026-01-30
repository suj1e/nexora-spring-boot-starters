package com.nexora.kafka.publisher;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests for {@link EventPublisher}.
 */
@DisplayName("EventPublisher Tests")
@ExtendWith(MockitoExtension.class)
class EventPublisherTest {

    @Mock
    private KafkaTemplate<String, String> kafkaTemplate;

    private ObjectMapper objectMapper;
    private EventPublisher eventPublisher;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        eventPublisher = new EventPublisher(kafkaTemplate, objectMapper);
    }

    @Test
    @DisplayName("Publish should send event to Kafka with correct parameters")
    void testPublishSendsEvent() {
        String eventType = "USER_CREATED";
        String bizId = "user123";
        String topic = "user-events";
        Map<String, Object> payload = new HashMap<>();
        payload.put("userId", 123L);
        payload.put("username", "testuser");

        when(kafkaTemplate.send(eq(topic), eq(bizId), anyString()))
            .thenReturn(CompletableFuture.completedFuture(mock(SendResult.class)));

        eventPublisher.publish(eventType, bizId, topic, payload);

        ArgumentCaptor<String> payloadCaptor = ArgumentCaptor.forClass(String.class);
        verify(kafkaTemplate).send(eq(topic), eq(bizId), payloadCaptor.capture());

        String capturedPayload = payloadCaptor.getValue();
        assertAll("Published payload",
            () -> assertTrue(capturedPayload.contains("\"userId\":123")),
            () -> assertTrue(capturedPayload.contains("\"username\":\"testuser\""))
        );
    }

    @Test
    @DisplayName("Publish should serialize payload to JSON")
    void testPublishSerializesPayload() throws Exception {
        String eventType = "TEST_EVENT";
        String bizId = "test123";
        String topic = "test-topic";
        Map<String, Object> payload = new HashMap<>();
        payload.put("key1", "value1");
        payload.put("key2", 42);
        payload.put("nested", Map.of("innerKey", "innerValue"));

        when(kafkaTemplate.send(eq(topic), eq(bizId), anyString()))
            .thenReturn(CompletableFuture.completedFuture(mock(SendResult.class)));

        eventPublisher.publish(eventType, bizId, topic, payload);

        ArgumentCaptor<String> jsonCaptor = ArgumentCaptor.forClass(String.class);
        verify(kafkaTemplate).send(eq(topic), eq(bizId), jsonCaptor.capture());

        Map<String, Object> deserialized = objectMapper.readValue(
            jsonCaptor.getValue(),
            objectMapper.getTypeFactory().constructMapType(Map.class, String.class, Object.class)
        );

        assertEquals("value1", deserialized.get("key1"));
        assertEquals(42, deserialized.get("key2"));
    }

    @Test
    @DisplayName("PublishUserEvent should construct payload with user fields")
    void testPublishUserEvent() {
        String eventType = "USER_UPDATED";
        Long bizId = 456L;
        String topic = "user-events";
        String username = "testuser";
        String email = "test@example.com";
        String name = "Test User";
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("department", "engineering");

        when(kafkaTemplate.send(eq(topic), eq(String.valueOf(bizId)), anyString()))
            .thenReturn(CompletableFuture.completedFuture(mock(SendResult.class)));

        eventPublisher.publishUserEvent(eventType, bizId, topic, username, email, name, metadata);

        ArgumentCaptor<String> payloadCaptor = ArgumentCaptor.forClass(String.class);
        verify(kafkaTemplate).send(eq(topic), eq(String.valueOf(bizId)), payloadCaptor.capture());

        String capturedPayload = payloadCaptor.getValue();
        assertAll("User event payload",
            () -> assertTrue(capturedPayload.contains("\"userId\":456")),
            () -> assertTrue(capturedPayload.contains("\"username\":\"testuser\"")),
            () -> assertTrue(capturedPayload.contains("\"email\":\"test@example.com\"")),
            () -> assertTrue(capturedPayload.contains("\"name\":\"Test User\"")),
            () -> assertTrue(capturedPayload.contains("\"department\":\"engineering\""))
        );
    }

    @Test
    @DisplayName("PublishUserEvent should work without metadata")
    void testPublishUserEventWithoutMetadata() {
        String eventType = "USER_DELETED";
        Long bizId = 789L;
        String topic = "user-events";
        String username = "deleteduser";
        String email = "deleted@example.com";
        String name = "Deleted User";

        when(kafkaTemplate.send(eq(topic), eq(String.valueOf(bizId)), anyString()))
            .thenReturn(CompletableFuture.completedFuture(mock(SendResult.class)));

        eventPublisher.publishUserEvent(eventType, bizId, topic, username, email, name, null);

        ArgumentCaptor<String> payloadCaptor = ArgumentCaptor.forClass(String.class);
        verify(kafkaTemplate).send(eq(topic), eq(String.valueOf(bizId)), payloadCaptor.capture());

        String capturedPayload = payloadCaptor.getValue();
        assertAll("User event without metadata",
            () -> assertTrue(capturedPayload.contains("\"userId\":789")),
            () -> assertTrue(capturedPayload.contains("\"username\":\"deleteduser\"")),
            () -> assertTrue(capturedPayload.contains("\"email\":\"deleted@example.com\"")),
            () -> assertTrue(capturedPayload.contains("\"name\":\"Deleted User\""))
        );
    }

    @Test
    @DisplayName("Publish should handle empty payload map")
    void testPublishWithEmptyPayload() {
        String eventType = "EMPTY_EVENT";
        String bizId = "empty123";
        String topic = "empty-topic";
        Map<String, Object> payload = new HashMap<>();

        when(kafkaTemplate.send(eq(topic), eq(bizId), anyString()))
            .thenReturn(CompletableFuture.completedFuture(mock(SendResult.class)));

        eventPublisher.publish(eventType, bizId, topic, payload);

        ArgumentCaptor<String> payloadCaptor = ArgumentCaptor.forClass(String.class);
        verify(kafkaTemplate).send(eq(topic), eq(bizId), payloadCaptor.capture());

        assertEquals("{}", payloadCaptor.getValue(), "Empty map should serialize to empty JSON object");
    }

    @Test
    @DisplayName("Publish should wrap exception on failure")
    void testPublishWrapsException() {
        String eventType = "FAIL_EVENT";
        String bizId = "fail123";
        String topic = "fail-topic";
        Map<String, Object> payload = new HashMap<>();
        payload.put("data", "test");

        when(kafkaTemplate.send(anyString(), anyString(), anyString()))
            .thenThrow(new RuntimeException("Kafka connection failed"));

        RuntimeException exception = assertThrows(RuntimeException.class,
            () -> eventPublisher.publish(eventType, bizId, topic, payload));

        assertTrue(exception.getMessage().contains("Failed to publish event"));
        assertEquals(RuntimeException.class, exception.getCause().getClass());
    }

    @Test
    @DisplayName("Publish should handle null metadata in user event")
    void testPublishUserEventWithNullMetadata() {
        when(kafkaTemplate.send(anyString(), anyString(), anyString()))
            .thenReturn(CompletableFuture.completedFuture(mock(SendResult.class)));

        assertDoesNotThrow(() ->
            eventPublisher.publishUserEvent("EVENT", 1L, "topic", "user", "email", "name", null)
        );
    }
}
