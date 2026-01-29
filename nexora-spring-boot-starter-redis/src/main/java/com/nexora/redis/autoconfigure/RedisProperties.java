package com.nexora.redis.autoconfigure;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * Redis cache properties.
 *
 * <p>Configuration example:
 * <pre>
 * nexora.redis.enabled=true
 * nexora.redis.cache-default-ttl=30m
 * nexora.redis.cache-names=user:10m,role:5m,token-blacklist:30m
 * nexora.redis.use-cache-prefix=true
 * nexora.redis.key-prefix=myapp:
 * </pre>
 *
 * @author sujie
 */
@Data
@ConfigurationProperties(prefix = "nexora.redis")
public class RedisProperties {

    /**
     * Enable Redis caching.
     */
    private boolean enabled = true;

    /**
     * Default TTL for cache entries.
     */
    private Duration cacheDefaultTtl = Duration.ofMinutes(30);

    /**
     * Cache name to TTL mapping.
     * Format: cache-name:duration,cache-name2:duration2
     * Duration format: 30m, 1h, etc.
     */
    private Map<String, Duration> cacheTtlMappings = new HashMap<>();

    /**
     * Use key prefix for cache entries.
     */
    private boolean useCachePrefix = true;

    /**
     * Key prefix for all cache entries.
     */
    private String keyPrefix = "";

    /**
     * Enable null values caching.
     */
    private boolean cacheNullValues = true;

    /**
     * Use Caffeine as local cache (L1 cache).
     */
    private boolean enableCaffeine = true;

    /**
     * Caffeine cache specification.
     */
    private String caffeineSpec = "maximumSize=1000,expireAfterWrite=5m";
}
