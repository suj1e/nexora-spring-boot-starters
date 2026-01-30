package com.nexora.resilience.listener;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.event.CircuitBreakerEvent;
import io.github.resilience4j.circuitbreaker.event.CircuitBreakerOnFailureRateExceededEvent;
import io.github.resilience4j.circuitbreaker.event.CircuitBreakerOnStateTransitionEvent;
import io.github.resilience4j.core.EventConsumer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.stereotype.Component;

/**
 * Circuit breaker event logger.
 *
 * <p>Logs circuit breaker state transitions and important events for monitoring.
 *
 * @author sujie
 */
@Slf4j
@Component
@ConditionalOnClass(CircuitBreaker.class)
public class CircuitBreakerEventLogger implements EventConsumer<CircuitBreakerEvent> {

    @Override
    public void consumeEvent(CircuitBreakerEvent event) {
        String circuitBreakerName = event.getCircuitBreakerName();

        log.info("Circuit Breaker Event: name={}, type={}", circuitBreakerName, event.getEventType());

        // Log state transitions
        if (event instanceof CircuitBreakerOnStateTransitionEvent transitionEvent) {
            log.warn("Circuit Breaker State Transition: name={}, from={}, to={}",
                    circuitBreakerName,
                    transitionEvent.getStateTransition(),
                    transitionEvent.getStateTransition().toString());
        }

        // Log failure rate exceeded
        if (event instanceof CircuitBreakerOnFailureRateExceededEvent failureRateEvent) {
            log.error("Circuit Breaker Failure Rate Exceeded: name={}, failureRate={}%",
                    circuitBreakerName,
                    failureRateEvent.getFailureRate());
        }
    }
}
