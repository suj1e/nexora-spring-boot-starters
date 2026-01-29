package com.nexora.web.model;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Unified API response format.
 *
 * <p>Automatically wraps all controller responses:
 * <pre>
 * // Controller returns:
 * return user;
 *
 * // Client receives:
 * {
 *   "code": 200,
 *   "message": "success",
 *   "data": { ... },
 *   "timestamp": "2024-01-28T10:00:00Z"
 * }
 * </pre>
 *
 * @param <T> data type
 * @author sujie
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record Result<T>(
    Integer code,
    String message,
    T data,
    String timestamp
) {

    /**
     * Success response with data.
     */
    public static <T> Result<T> success(T data) {
        return new Result<>(
            200,
            "success",
            data,
            java.time.Instant.now().toString()
        );
    }

    /**
     * Success response without data.
     */
    public static <T> Result<T> success() {
        return success(null);
    }

    /**
     * Error response.
     */
    public static <T> Result<T> error(Integer code, String message) {
        return new Result<>(
            code,
            message,
            null,
            java.time.Instant.now().toString()
        );
    }

    /**
     * Error response with data.
     */
    public static <T> Result<T> error(Integer code, String message, T data) {
        return new Result<>(
            code,
            message,
            data,
            java.time.Instant.now().toString()
        );
    }

    /**
     * Created response (201).
     */
    public static <T> Result<T> created(T data) {
        return new Result<>(
            201,
            "created",
            data,
            java.time.Instant.now().toString()
        );
    }

    /**
     * No content response (204).
     */
    public static <T> Result<T> noContent() {
        return new Result<>(
            204,
            "no content",
            null,
            java.time.Instant.now().toString()
        );
    }
}
