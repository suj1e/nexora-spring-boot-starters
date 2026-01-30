package com.nexora.resilience.listener;

import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.event.RetryEvent;
import io.github.resilience4j.retry.event.RetryOnErrorEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.stereotype.Component;

/**
 * Retry event logger.
 *
 * <p>Logs retry attempts and failures for monitoring.
 *
 * @author sujie
 */
@Slf4j
@Component
@ConditionalOnClass(Retry.class)
public class RetryEventLogger implements io.github.resilience4j.core.EventConsumer<RetryEvent> {

    @Override
    public void consumeEvent(RetryEvent event) {
        String retryName = event.getName();

        log.info("Retry Event: name={}, type={}", retryName, event.getEventType());

        // Log retry attempts with error
        if (event instanceof RetryOnErrorEvent errorEvent) {
            log.warn("Retry Attempt: name={}, attempt={}, error={}",
                    retryName,
                    errorEvent.getNumberOfRetryAttempts(),
                    errorEvent.getLastThrowable().getMessage());
        }
    }
}
