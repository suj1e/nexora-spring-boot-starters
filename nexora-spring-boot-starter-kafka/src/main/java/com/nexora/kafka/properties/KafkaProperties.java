package com.nexora.kafka.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Kafka configuration properties.
 *
 * @author sujie
 */
@ConfigurationProperties(prefix = "nexora.kafka")
public class KafkaProperties {

    /**
     * DLQ configuration.
     */
    private Dlq dlq = new Dlq();

    /**
     * Outbox configuration.
     */
    private Outbox outbox = new Outbox();

    public Dlq getDlq() {
        return dlq;
    }

    public void setDlq(Dlq dlq) {
        this.dlq = dlq;
    }

    public Outbox getOutbox() {
        return outbox;
    }

    public void setOutbox(Outbox outbox) {
        this.outbox = outbox;
    }

    public static class Dlq {
        /**
         * Enable DLQ support.
         */
        private boolean enabled = true;

        /**
         * Max retry attempts before sending to DLQ.
         */
        private int retryAttempts = 3;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public int getRetryAttempts() {
            return retryAttempts;
        }

        public void setRetryAttempts(int retryAttempts) {
            this.retryAttempts = retryAttempts;
        }
    }

    public static class Outbox {
        /**
         * Enable Outbox pattern support.
         */
        private boolean enabled = false;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }
    }
}
