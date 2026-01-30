package com.nexora.resilience.autoconfigure;

import com.nexora.resilience.listener.CircuitBreakerEventLogger;
import com.nexora.resilience.listener.RetryEventLogger;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.retry.RetryRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;

/**
 * Event listener auto-configuration for Resilience4j.
 *
 * <p>Registers event listeners to Circuit Breaker and Retry registries.
 *
 * @author sujie
 */
@Slf4j
@Configuration
@ConditionalOnClass(name = {"io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry",
        "io.github.resilience4j.retry.RetryRegistry"})
public class EventListenerAutoConfiguration {

    private final CircuitBreakerRegistry circuitBreakerRegistry;
    private final RetryRegistry retryRegistry;
    private final CircuitBreakerEventLogger circuitBreakerEventLogger;
    private final RetryEventLogger retryEventLogger;

    public EventListenerAutoConfiguration(
            CircuitBreakerRegistry circuitBreakerRegistry,
            RetryRegistry retryRegistry,
            CircuitBreakerEventLogger circuitBreakerEventLogger,
            RetryEventLogger retryEventLogger) {
        this.circuitBreakerRegistry = circuitBreakerRegistry;
        this.retryRegistry = retryRegistry;
        this.circuitBreakerEventLogger = circuitBreakerEventLogger;
        this.retryEventLogger = retryEventLogger;
    }

    /**
     * Register event listeners to registries.
     */
    @PostConstruct
    public void registerEventListeners() {
        // Register circuit breaker event listener
        circuitBreakerRegistry.getAllCircuitBreakers()
                .forEach(cb -> cb.getEventPublisher().onEvent(circuitBreakerEventLogger));

        // Register retry event listener
        retryRegistry.getAllRetries()
                .forEach(retry -> retry.getEventPublisher().onEvent(retryEventLogger));

        log.info("Registered Resilience4j event listeners");
    }
}
