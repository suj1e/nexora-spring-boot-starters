package com.nexora.id;

import lombok.extern.slf4j.Slf4j;

/**
 * Snowflake ID generator.
 *
 * <p>Structure of ID (64 bits):
 * <pre>
 * |--------|--------|--------|--------|
 * | 1 bit  | 41 bits |  5 bits | 17 bits |
 * | sign   | timestamp|datacenter| sequence |
 * |--------|--------|--------|--------|
 * </pre>
 *
 * <ul>
 *   <li>1 bit: Always 0 (sign bit, unused)</li>
 *   <li>41 bits: Milliseconds since epoch (69 years)</li>
 *   <li>5 bits: Datacenter ID (0-31)</li>
 *   <li>5 bits: Worker ID (0-31)</li>
 *   <li>12 bits: Sequence number (0-4095)</li>
 * </ul>
 *
 * @author sujie
 */
@Slf4j
public class SnowflakeIdGenerator {

    // Constants for bit shifting
    private static final int WORKER_ID_SHIFT = 12;
    private static final int DATACENTER_ID_SHIFT = 17;
    private static final int TIMESTAMP_SHIFT = 22;

    // Max values
    private static final long MAX_DATACENTER_ID = 31L;
    private static final long MAX_WORKER_ID = 31L;
    private static final long MAX_SEQUENCE = 4095L;

    private final long epoch;
    private final long workerId;
    private final long datacenterId;

    private long sequence = 0L;
    private long lastTimestamp = -1L;

    /**
     * Creates a new Snowflake ID generator.
     *
     * @param epoch        the epoch timestamp (milliseconds since Unix epoch)
     * @param workerId     the worker ID (0-31)
     * @param datacenterId the datacenter ID (0-31)
     */
    public SnowflakeIdGenerator(long epoch, long workerId, long datacenterId) {
        if (workerId < 0 || workerId > MAX_WORKER_ID) {
            throw new IllegalArgumentException(
                String.format("Worker ID must be between 0 and %d", MAX_WORKER_ID));
        }
        if (datacenterId < 0 || datacenterId > MAX_DATACENTER_ID) {
            throw new IllegalArgumentException(
                String.format("Datacenter ID must be between 0 and %d", MAX_DATACENTER_ID));
        }
        if (epoch < 0) {
            throw new IllegalArgumentException("Epoch must be positive");
        }

        this.epoch = epoch;
        this.workerId = workerId;
        this.datacenterId = datacenterId;

        log.info("Snowflake ID generator initialized with workerId={}, datacenterId={}, epoch={}",
            workerId, datacenterId, epoch);
    }

    /**
     * Generates a new unique ID.
     *
     * @return a new unique ID
     */
    public synchronized long nextId() {
        long timestamp = currentTimeMillis();

        // Clock moved backwards
        if (timestamp < lastTimestamp) {
            throw new IllegalStateException(
                String.format("Clock moved backwards. Refusing to generate ID for %d milliseconds",
                    lastTimestamp - timestamp));
        }

        // Same millisecond
        if (timestamp == lastTimestamp) {
            sequence = (sequence + 1) & MAX_SEQUENCE;
            // Sequence overflow
            if (sequence == 0) {
                timestamp = waitNextMillis(lastTimestamp);
            }
        } else {
            // New millisecond, reset sequence
            sequence = 0L;
        }

        lastTimestamp = timestamp;

        // Generate ID
        return ((timestamp - epoch) << TIMESTAMP_SHIFT)
            | (datacenterId << DATACENTER_ID_SHIFT)
            | (workerId << WORKER_ID_SHIFT)
            | sequence;
    }

    /**
     * Parses a Snowflake ID to extract information.
     *
     * @param id the Snowflake ID to parse
     * @return an array containing [timestamp, datacenterId, workerId, sequence]
     */
    public long[] parseId(long id) {
        long timestamp = ((id >> TIMESTAMP_SHIFT) & ~0L) + epoch;
        long datacenterId = (id >> DATACENTER_ID_SHIFT) & MAX_DATACENTER_ID;
        long workerId = (id >> WORKER_ID_SHIFT) & MAX_WORKER_ID;
        long sequence = id & MAX_SEQUENCE;
        return new long[]{timestamp, datacenterId, workerId, sequence};
    }

    /**
     * Gets the current timestamp in milliseconds.
     *
     * @return current timestamp in milliseconds
     */
    protected long currentTimeMillis() {
        return System.currentTimeMillis();
    }

    /**
     * Waits until the next millisecond.
     *
     * @param lastTimestamp the last timestamp
     * @return the new timestamp
     */
    private long waitNextMillis(long lastTimestamp) {
        long timestamp = currentTimeMillis();
        while (timestamp <= lastTimestamp) {
            timestamp = currentTimeMillis();
        }
        return timestamp;
    }
}
