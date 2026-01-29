package com.nexora.id;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Snowflake ID generator configuration properties.
 *
 * @author sujie
 */
@ConfigurationProperties(prefix = "snowflake")
public class SnowflakeProperties {

    /**
     * Worker ID (0-31).
     * Each instance should have a unique worker ID.
     */
    private int workerId = 0;

    /**
     * Datacenter ID (0-31).
     * Each datacenter should have a unique datacenter ID.
     */
    private int datacenterId = 0;

    /**
     * Epoch timestamp (milliseconds since Unix epoch).
     * Default: 2024-01-01 00:00:00 UTC (1704067200000L)
     */
    private long epoch = 1704067200000L;

    public int getWorkerId() {
        return workerId;
    }

    public void setWorkerId(int workerId) {
        if (workerId < 0 || workerId > 31) {
            throw new IllegalArgumentException("Worker ID must be between 0 and 31");
        }
        this.workerId = workerId;
    }

    public int getDatacenterId() {
        return datacenterId;
    }

    public void setDatacenterId(int datacenterId) {
        if (datacenterId < 0 || datacenterId > 31) {
            throw new IllegalArgumentException("Datacenter ID must be between 0 and 31");
        }
        this.datacenterId = datacenterId;
    }

    public long getEpoch() {
        return epoch;
    }

    public void setEpoch(long epoch) {
        if (epoch < 0) {
            throw new IllegalArgumentException("Epoch must be positive");
        }
        this.epoch = epoch;
    }
}
