package com.nexora.kafka.outbox;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link OutboxStatus}.
 */
@DisplayName("OutboxStatus Tests")
class OutboxStatusTest {

    @Test
    @DisplayName("Enum should have exactly three values")
    void testEnumHasThreeValues() {
        OutboxStatus[] values = OutboxStatus.values();

        assertEquals(3, values.length);
    }

    @Test
    @DisplayName("Enum should contain NEW, SENT, and FAILED")
    void testEnumValues() {
        assertAll("Enum values exist",
            () -> assertNotNull(OutboxStatus.valueOf("NEW")),
            () -> assertNotNull(OutboxStatus.valueOf("SENT")),
            () -> assertNotNull(OutboxStatus.valueOf("FAILED"))
        );
    }

    @Test
    @DisplayName("NEW status should represent unprocessed events")
    void testNewStatus() {
        OutboxStatus status = OutboxStatus.NEW;
        assertEquals("NEW", status.name());
    }

    @Test
    @DisplayName("SENT status should represent successfully published events")
    void testSentStatus() {
        OutboxStatus status = OutboxStatus.SENT;
        assertEquals("SENT", status.name());
    }

    @Test
    @DisplayName("FAILED status should represent failed events")
    void testFailedStatus() {
        OutboxStatus status = OutboxStatus.FAILED;
        assertEquals("FAILED", status.name());
    }

    @Test
    @DisplayName("Statuses should be comparable")
    void testStatusesComparable() {
        OutboxStatus newStatus = OutboxStatus.NEW;
        OutboxStatus sentStatus = OutboxStatus.SENT;
        OutboxStatus failedStatus = OutboxStatus.FAILED;

        assertAll("Statuses are comparable",
            () -> assertEquals(newStatus, OutboxStatus.NEW),
            () -> assertEquals(sentStatus, OutboxStatus.SENT),
            () -> assertEquals(failedStatus, OutboxStatus.FAILED),
            () -> assertNotEquals(newStatus, sentStatus),
            () -> assertNotEquals(sentStatus, failedStatus)
        );
    }

    @Test
    @DisplayName("valueOf should throw exception for invalid status name")
    void testValueOfInvalidName() {
        assertThrows(IllegalArgumentException.class, () -> OutboxStatus.valueOf("INVALID"));
    }
}
