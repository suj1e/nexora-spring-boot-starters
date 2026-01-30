package com.nexora.web.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link Result}.
 */
@DisplayName("Result Tests")
class ResultTest {

    @Test
    @DisplayName("Success result should contain data and correct status code")
    void testSuccessResultWithData() {
        Result<String> result = Result.success("test data");

        assertAll("Success result",
            () -> assertEquals(200, result.code(), "Code should be 200"),
            () -> assertEquals("test data", result.data(), "Data should match"),
            () -> assertNotNull(result.timestamp(), "Timestamp should be generated"),
            () -> assertEquals("success", result.message(), "Message should be 'success'")
        );
    }

    @Test
    @DisplayName("Success result without data should have null data field")
    void testSuccessResultWithoutData() {
        Result<String> result = Result.success();

        assertAll("Success result without data",
            () -> assertEquals(200, result.code()),
            () -> assertNull(result.data()),
            () -> assertNotNull(result.timestamp())
        );
    }

    @Test
    @DisplayName("Error result should have error code and message")
    void testErrorResult() {
        Result<String> result = Result.error(404, "Not found");

        assertAll("Error result",
            () -> assertEquals(404, result.code()),
            () -> assertEquals("Not found", result.message()),
            () -> assertNull(result.data())
        );
    }

    @Test
    @DisplayName("Error result with custom code and data")
    void testErrorResultWithData() {
        String errorDetails = "Invalid parameter: userId must not be null";
        Result<String> result = Result.error(400, errorDetails, errorDetails);

        assertAll("Error result with data",
            () -> assertEquals(400, result.code()),
            () -> assertEquals(errorDetails, result.message()),
            () -> assertEquals(errorDetails, result.data())
        );
    }

    @Test
    @DisplayName("Created result should have 201 status")
    void testCreatedResult() {
        String resource = "/api/users/123";
        Result<String> result = Result.created(resource);

        assertAll("Created result",
            () -> assertEquals(201, result.code()),
            () -> assertEquals("created", result.message()),
            () -> assertEquals(resource, result.data())
        );
    }

    @Test
    @DisplayName("No content result should have 204 status")
    void testNoContentResult() {
        Result<Void> result = Result.noContent();

        assertAll("No content result",
            () -> assertEquals(204, result.code()),
            () -> assertEquals("no content", result.message()),
            () -> assertNull(result.data())
        );
    }

    @Test
    @DisplayName("Record should be immutable with valid components")
    void testRecordComponents() {
        Integer code = 200;
        String message = "success";
        String data = "payload";
        String timestamp = "2024-01-30T12:00:00Z";

        Result<String> result = new Result<>(code, message, data, timestamp);

        assertAll("Record components",
            () -> assertEquals(code, result.code()),
            () -> assertEquals(message, result.message()),
            () -> assertEquals(data, result.data()),
            () -> assertEquals(timestamp, result.timestamp())
        );
    }

    @Test
    @DisplayName("Multiple success results should have independent timestamps")
    void testIndependentTimestamps() {
        Result<String> result1 = Result.success("data1");
        Result<String> result2 = Result.success("data2");

        assertNotEquals("Timestamps should be independent",
                         result1.timestamp(),
                         result2.timestamp());
    }
}
