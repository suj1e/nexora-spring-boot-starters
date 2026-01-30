package com.nexora.resilience.autoconfigure;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link ResilienceProperties}.
 */
@DisplayName("ResilienceProperties Tests")
class ResiliencePropertiesTest {

    @Test
    @DisplayName("Default circuit breaker properties should have expected values")
    void testDefaultCircuitBreakerProperties() {
        ResilienceProperties properties = new ResilienceProperties();
        ResilienceProperties.CircuitBreaker cb = properties.getCircuitBreaker();

        assertAll("Default circuit breaker properties",
            () -> assertTrue(cb.isEnabled(), "Circuit breaker should be enabled by default"),
            () -> assertEquals(50f, cb.getFailureRateThreshold(), "Failure rate threshold should be 50%"),
            () -> assertEquals(Duration.ofSeconds(10), cb.getWaitDurationInOpenState(),
                "Wait duration in open state should be 10s"),
            () -> assertEquals(3, cb.getPermittedNumberOfCallsInHalfOpenState(),
                "Permitted calls in half-open state should be 3"),
            () -> assertEquals(10, cb.getSlidingWindowSize(), "Sliding window size should be 10"),
            () -> assertEquals(5, cb.getMinimumNumberOfCalls(), "Minimum number of calls should be 5")
        );
    }

    @Test
    @DisplayName("Default retry properties should have expected values")
    void testDefaultRetryProperties() {
        ResilienceProperties properties = new ResilienceProperties();
        ResilienceProperties.Retry retry = properties.getRetry();

        assertAll("Default retry properties",
            () -> assertTrue(retry.isEnabled(), "Retry should be enabled by default"),
            () -> assertEquals(3, retry.getMaxAttempts(), "Max attempts should be 3"),
            () -> assertEquals(Duration.ofSeconds(1), retry.getWaitDuration(),
                "Wait duration should be 1s"),
            () -> assertFalse(retry.isEnableExponentialBackoff(),
                "Exponential backoff should be disabled by default"),
            () -> assertEquals(2.0, retry.getExponentialBackoffMultiplier(),
                "Exponential backoff multiplier should be 2.0")
        );
    }

    @Test
    @DisplayName("Default time limiter properties should have expected values")
    void testDefaultTimeLimiterProperties() {
        ResilienceProperties properties = new ResilienceProperties();
        ResilienceProperties.TimeLimiter tl = properties.getTimeLimiter();

        assertAll("Default time limiter properties",
            () -> assertFalse(tl.isEnabled(), "Time limiter should be disabled by default"),
            () -> assertEquals(Duration.ofSeconds(5), tl.getTimeoutDuration(),
                "Timeout duration should be 5s")
        );
    }

    @Test
    @DisplayName("Default rate limiter properties should have expected values")
    void testDefaultRateLimiterProperties() {
        ResilienceProperties properties = new ResilienceProperties();
        ResilienceProperties.RateLimiter rl = properties.getRateLimiter();

        assertAll("Default rate limiter properties",
            () -> assertFalse(rl.isEnabled(), "Rate limiter should be disabled by default"),
            () -> assertEquals(10, rl.getLimitForPeriod(), "Limit for period should be 10"),
            () -> assertEquals(Duration.ofSeconds(1), rl.getLimitRefreshPeriod(),
                "Limit refresh period should be 1s"),
            () -> assertEquals(Duration.ofSeconds(5), rl.getTimeoutDuration(),
                "Timeout duration should be 5s")
        );
    }

    @Test
    @DisplayName("Should be able to customize circuit breaker properties")
    void testCustomizeCircuitBreakerProperties() {
        ResilienceProperties properties = new ResilienceProperties();
        ResilienceProperties.CircuitBreaker cb = properties.getCircuitBreaker();

        cb.setEnabled(false);
        cb.setFailureRateThreshold(75f);
        cb.setWaitDurationInOpenState(Duration.ofMinutes(1));
        cb.setPermittedNumberOfCallsInHalfOpenState(5);
        cb.setSlidingWindowSize(20);
        cb.setMinimumNumberOfCalls(10);

        assertAll("Custom circuit breaker properties",
            () -> assertFalse(cb.isEnabled()),
            () -> assertEquals(75f, cb.getFailureRateThreshold()),
            () -> assertEquals(Duration.ofMinutes(1), cb.getWaitDurationInOpenState()),
            () -> assertEquals(5, cb.getPermittedNumberOfCallsInHalfOpenState()),
            () -> assertEquals(20, cb.getSlidingWindowSize()),
            () -> assertEquals(10, cb.getMinimumNumberOfCalls())
        );
    }

    @Test
    @DisplayName("Should be able to customize retry properties")
    void testCustomizeRetryProperties() {
        ResilienceProperties properties = new ResilienceProperties();
        ResilienceProperties.Retry retry = properties.getRetry();

        retry.setEnabled(false);
        retry.setMaxAttempts(5);
        retry.setWaitDuration(Duration.ofMillis(500));
        retry.setEnableExponentialBackoff(true);
        retry.setExponentialBackoffMultiplier(3.0);

        assertAll("Custom retry properties",
            () -> assertFalse(retry.isEnabled()),
            () -> assertEquals(5, retry.getMaxAttempts()),
            () -> assertEquals(Duration.ofMillis(500), retry.getWaitDuration()),
            () -> assertTrue(retry.isEnableExponentialBackoff()),
            () -> assertEquals(3.0, retry.getExponentialBackoffMultiplier())
        );
    }

    @Test
    @DisplayName("Circuit breaker instance configs should be mutable")
    void testCircuitBreakerInstanceConfigs() {
        ResilienceProperties properties = new ResilienceProperties();
        ResilienceProperties.CircuitBreaker cb = properties.getCircuitBreaker();
        Map<String, Duration> configs = new HashMap<>();
        configs.put("userService", Duration.ofSeconds(30));
        configs.put("orderService", Duration.ofMinutes(1));

        cb.setInstanceConfigs(configs);

        assertAll("Instance configs",
            () -> assertEquals(2, cb.getInstanceConfigs().size()),
            () -> assertEquals(Duration.ofSeconds(30), cb.getInstanceConfigs().get("userService")),
            () -> assertEquals(Duration.ofMinutes(1), cb.getInstanceConfigs().get("orderService"))
        );
    }

    @Test
    @DisplayName("Instance configs should initialize as empty map")
    void testInstanceConfigsInitialization() {
        ResilienceProperties properties = new ResilienceProperties();
        ResilienceProperties.CircuitBreaker cb = properties.getCircuitBreaker();

        assertNotNull(cb.getInstanceConfigs(), "Instance configs should not be null");
        assertTrue(cb.getInstanceConfigs().isEmpty(), "Instance configs should be empty by default");
    }

    @Test
    @DisplayName("Should be able to customize time limiter properties")
    void testCustomizeTimeLimiterProperties() {
        ResilienceProperties properties = new ResilienceProperties();
        ResilienceProperties.TimeLimiter tl = properties.getTimeLimiter();

        tl.setEnabled(true);
        tl.setTimeoutDuration(Duration.ofSeconds(10));

        assertAll("Custom time limiter properties",
            () -> assertTrue(tl.isEnabled()),
            () -> assertEquals(Duration.ofSeconds(10), tl.getTimeoutDuration())
        );
    }

    @Test
    @DisplayName("Should be able to customize rate limiter properties")
    void testCustomizeRateLimiterProperties() {
        ResilienceProperties properties = new ResilienceProperties();
        ResilienceProperties.RateLimiter rl = properties.getRateLimiter();

        rl.setEnabled(true);
        rl.setLimitForPeriod(100);
        rl.setLimitRefreshPeriod(Duration.ofMinutes(1));
        rl.setTimeoutDuration(Duration.ofSeconds(30));

        assertAll("Custom rate limiter properties",
            () -> assertTrue(rl.isEnabled()),
            () -> assertEquals(100, rl.getLimitForPeriod()),
            () -> assertEquals(Duration.ofMinutes(1), rl.getLimitRefreshPeriod()),
            () -> assertEquals(Duration.ofSeconds(30), rl.getTimeoutDuration())
        );
    }
}
