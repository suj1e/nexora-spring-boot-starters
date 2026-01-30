package com.nexora.redis.autoconfigure;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link RedisProperties}.
 */
@DisplayName("RedisProperties Tests")
class RedisPropertiesTest {

    @Test
    @DisplayName("Default properties should have expected default values")
    void testDefaultProperties() {
        RedisProperties properties = new RedisProperties();

        assertAll("Default Redis properties",
            () -> assertTrue(properties.isEnabled(), "Should be enabled by default"),
            () -> assertEquals(Duration.ofMinutes(30), properties.getCacheDefaultTtl(),
                "Default TTL should be 30 minutes"),
            () -> assertTrue(properties.isUseCachePrefix(), "Should use cache prefix by default"),
            () -> assertEquals("", properties.getKeyPrefix(), "Key prefix should be empty by default"),
            () -> assertTrue(properties.isCacheNullValues(), "Should cache null values by default"),
            () -> assertTrue(properties.isEnableCaffeine(), "Caffeine should be enabled by default"),
            () -> assertEquals("maximumSize=1000,expireAfterWrite=5m", properties.getCaffeineSpec(),
                "Caffeine spec should have default value")
        );
    }

    @Test
    @DisplayName("Cache TTL mappings should be mutable")
    void testCacheTtlMappings() {
        RedisProperties properties = new RedisProperties();
        Map<String, Duration> mappings = new HashMap<>();
        mappings.put("userCache", Duration.ofMinutes(10));
        mappings.put("roleCache", Duration.ofHours(1));

        properties.setCacheTtlMappings(mappings);

        assertAll("Cache TTL mappings",
            () -> assertEquals(2, properties.getCacheTtlMappings().size()),
            () -> assertEquals(Duration.ofMinutes(10), properties.getCacheTtlMappings().get("userCache")),
            () -> assertEquals(Duration.ofHours(1), properties.getCacheTtlMappings().get("roleCache"))
        );
    }

    @Test
    @DisplayName("Should be able to set all properties")
    void testSetAllProperties() {
        RedisProperties properties = new RedisProperties();
        Map<String, Duration> mappings = new HashMap<>();
        mappings.put("test", Duration.ofMinutes(5));

        properties.setEnabled(false);
        properties.setCacheDefaultTtl(Duration.ofHours(1));
        properties.setCacheTtlMappings(mappings);
        properties.setUseCachePrefix(false);
        properties.setKeyPrefix("custom:");
        properties.setCacheNullValues(false);
        properties.setEnableCaffeine(false);
        properties.setCaffeineSpec("maximumSize=500");

        assertAll("Custom properties",
            () -> assertFalse(properties.isEnabled()),
            () -> assertEquals(Duration.ofHours(1), properties.getCacheDefaultTtl()),
            () -> assertFalse(properties.isUseCachePrefix()),
            () -> assertEquals("custom:", properties.getKeyPrefix()),
            () -> assertFalse(properties.isCacheNullValues()),
            () -> assertFalse(properties.isEnableCaffeine()),
            () -> assertEquals("maximumSize=500", properties.getCaffeineSpec())
        );
    }

    @Test
    @DisplayName("Cache TTL mappings should initialize as empty map")
    void testCacheTtlMappingsInitialization() {
        RedisProperties properties = new RedisProperties();

        assertNotNull(properties.getCacheTtlMappings(), "Cache TTL mappings should not be null");
        assertTrue(properties.getCacheTtlMappings().isEmpty(), "Cache TTL mappings should be empty by default");
    }
}
