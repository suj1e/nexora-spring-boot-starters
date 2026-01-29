package com.nexora.resilience.autoconfigure;

import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.core.ConfigurationNotFoundException;
import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.retry.RetryRegistry;
import io.github.resilience4j.timelimiter.TimeLimiterConfig;
import io.github.resilience4j.timelimiter.TimeLimiterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * Resilience4j auto-configuration.
 *
 * <p>Features:
 * <ul>
 *   <li>Circuit Breaker - prevents cascading failures</li>
 *   <li>Retry - automatic retry on failure</li>
 *   <li>Time Limiter - timeout protection</li>
 *   <li>Rate Limiter - request throttling</li>
 * </ul>
 *
 * @author sujie
 */
@Slf4j
@Configuration
@ConditionalOnClass(name = {"io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry",
        "io.github.resilience4j.retry.RetryRegistry"})
@EnableConfigurationProperties(ResilienceProperties.class)
@ConditionalOnProperty(prefix = "nexora.resilience", name = "enabled", havingValue = "true", matchIfMissing = true)
public class ResilienceAutoConfiguration {

    private final ResilienceProperties properties;

    public ResilienceAutoConfiguration(ResilienceProperties properties) {
        this.properties = properties;
    }

    /**
     * Circuit Breaker Registry with default configuration.
     */
    @Bean
    public CircuitBreakerRegistry circuitBreakerRegistry() {
        CircuitBreakerConfig config = CircuitBreakerConfig.custom()
                .failureRateThreshold(properties.getCircuitBreaker().getFailureRateThreshold())
                .waitDurationInOpenState(properties.getCircuitBreaker().getWaitDurationInOpenState())
                .permittedNumberOfCallsInHalfOpenState(
                        properties.getCircuitBreaker().getPermittedNumberOfCallsInHalfOpenState())
                .slidingWindowSize(properties.getCircuitBreaker().getSlidingWindowSize())
                .minimumNumberOfCalls(properties.getCircuitBreaker().getMinimumNumberOfCalls())
                .build();

        CircuitBreakerRegistry registry = CircuitBreakerRegistry.of(config);

        // Add instance-specific configurations
        for (Map.Entry<String, Duration> entry : properties.getCircuitBreaker().getInstanceConfigs().entrySet()) {
            CircuitBreakerConfig instanceConfig = CircuitBreakerConfig.custom()
                    .failureRateThreshold(properties.getCircuitBreaker().getFailureRateThreshold())
                    .waitDurationInOpenState(entry.getValue())
                    .build();
            registry.addConfiguration(entry.getKey(), instanceConfig);
        }

        log.info("Initialized CircuitBreakerRegistry with failure rate threshold: {}%",
                properties.getCircuitBreaker().getFailureRateThreshold());

        return registry;
    }

    /**
     * Retry Registry with default configuration.
     */
    @Bean
    public RetryRegistry retryRegistry() {
        RetryConfig.Builder builder = RetryConfig.custom()
                .maxAttempts(properties.getRetry().getMaxAttempts())
                .waitDuration(properties.getRetry().getWaitDuration());

        if (properties.getRetry().isEnableExponentialBackoff()) {
            builder.intervalFunction(
                    io.github.resilience4j.core.IntervalFunction.ofExponentialBackoff(
                            properties.getRetry().getWaitDuration().toMillis(),
                            properties.getRetry().getExponentialBackoffMultiplier()
                    )
            );
        }

        RetryRegistry registry = RetryRegistry.of(builder.build());

        log.info("Initialized RetryRegistry with max attempts: {}", properties.getRetry().getMaxAttempts());

        return registry;
    }

    /**
     * Time Limiter Registry with default configuration.
     */
    @Bean
    @ConditionalOnProperty(prefix = "nexora.resilience.time-limiter", name = "enabled", havingValue = "true")
    public TimeLimiterRegistry timeLimiterRegistry() {
        TimeLimiterConfig config = TimeLimiterConfig.custom()
                .timeoutDuration(properties.getTimeLimiter().getTimeoutDuration())
                .build();

        log.info("Initialized TimeLimiterRegistry with timeout: {}", properties.getTimeLimiter().getTimeoutDuration());

        return TimeLimiterRegistry.of(config);
    }
}
