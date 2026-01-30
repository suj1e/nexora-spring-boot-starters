package com.nexora.redis.cache;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

import java.util.Optional;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests for {@link CacheHelper}.
 */
@DisplayName("CacheHelper Tests")
@ExtendWith(MockitoExtension.class)
class CacheHelperTest {

    @Mock
    private CacheManager cacheManager;

    @Mock
    private Cache cache;

    private CacheHelper cacheHelper;

    @BeforeEach
    void setUp() {
        cacheHelper = new CacheHelper(cacheManager);
    }

    @Test
    @DisplayName("getOrCompute should return cached value when present")
    void testGetOrComputeReturnsCachedValue() {
        String cacheName = "testCache";
        String key = "testKey";
        String cachedValue = "cachedValue";

        when(cacheManager.getCache(cacheName)).thenReturn(cache);
        when(cache.get(key)).thenReturn(() -> cachedValue);

        String result = cacheHelper.getOrCompute(cacheName, key, () -> "computedValue");

        assertEquals(cachedValue, result, "Should return cached value");
        verify(cache, never()).put(any(), any());
    }

    @Test
    @DisplayName("getOrCompute should compute and cache value when not present")
    void testGetOrComputeComputesAndCachesValue() {
        String cacheName = "testCache";
        String key = "testKey";
        String computedValue = "computedValue";

        when(cacheManager.getCache(cacheName)).thenReturn(cache);
        when(cache.get(key)).thenReturn(null);

        String result = cacheHelper.getOrCompute(cacheName, key, () -> computedValue);

        assertEquals(computedValue, result, "Should return computed value");
        verify(cache).put(key, computedValue);
    }

    @Test
    @DisplayName("getOrCompute should compute value when cache is null")
    void testGetOrComputeWithNullCache() {
        String cacheName = "nonExistentCache";
        String key = "testKey";
        String computedValue = "computedValue";

        when(cacheManager.getCache(cacheName)).thenReturn(null);

        String result = cacheHelper.getOrCompute(cacheName, key, () -> computedValue);

        assertEquals(computedValue, result, "Should return computed value when cache is null");
    }

    @Test
    @DisplayName("getOrCompute should invoke supplier only once when caching")
    void testGetOrComputeInvokesSupplierOnce() {
        String cacheName = "testCache";
        String key = "testKey";
        Supplier<String> supplier = mock(Supplier.class);

        when(cacheManager.getCache(cacheName)).thenReturn(cache);
        when(cache.get(key)).thenReturn(null);
        when(supplier.get()).thenReturn("value");

        cacheHelper.getOrCompute(cacheName, key, supplier);

        verify(supplier, times(1)).get();
    }

    @Test
    @DisplayName("getOptional should return empty when cache is null")
    void testGetOptionalWithNullCache() {
        when(cacheManager.getCache("nonExistentCache")).thenReturn(null);

        Optional<String> result = cacheHelper.getOptional("nonExistentCache", "key");

        assertTrue(result.isEmpty(), "Should return empty Optional when cache is null");
    }

    @Test
    @DisplayName("getOptional should return empty when value not in cache")
    void testGetOptionalWithNoValue() {
        when(cacheManager.getCache("testCache")).thenReturn(cache);
        when(cache.get("key")).thenReturn(null);

        Optional<String> result = cacheHelper.getOptional("testCache", "key");

        assertTrue(result.isEmpty(), "Should return empty Optional when value not cached");
    }

    @Test
    @DisplayName("getOptional should return value when present in cache")
    void testGetOptionalReturnsValue() {
        String cachedValue = "cachedValue";
        when(cacheManager.getCache("testCache")).thenReturn(cache);
        when(cache.get("key")).thenReturn(() -> cachedValue);

        Optional<String> result = cacheHelper.getOptional("testCache", "key");

        assertTrue(result.isPresent(), "Should return present Optional");
        assertEquals(cachedValue, result.get(), "Should return cached value");
    }

    @Test
    @DisplayName("getOptional should return empty for null cached value")
    void testGetOptionalWithNullCachedValue() {
        when(cacheManager.getCache("testCache")).thenReturn(cache);
        when(cache.get("key")).thenReturn(() -> null);

        Optional<String> result = cacheHelper.getOptional("testCache", "key");

        assertTrue(result.isEmpty(), "Should return empty Optional for null cached value");
    }

    @Test
    @DisplayName("evict should evict entry from cache")
    void testEvict() {
        when(cacheManager.getCache("testCache")).thenReturn(cache);

        cacheHelper.evict("testCache", "key");

        verify(cache).evict("key");
    }

    @Test
    @DisplayName("evict should do nothing when cache is null")
    void testEvictWithNullCache() {
        when(cacheManager.getCache("nonExistentCache")).thenReturn(null);

        assertDoesNotThrow(() -> cacheHelper.evict("nonExistentCache", "key"));
    }

    @Test
    @DisplayName("clear should clear all entries in cache")
    void testClear() {
        when(cacheManager.getCache("testCache")).thenReturn(cache);

        cacheHelper.clear("testCache");

        verify(cache).clear();
    }

    @Test
    @DisplayName("clear should do nothing when cache is null")
    void testClearWithNullCache() {
        when(cacheManager.getCache("nonExistentCache")).thenReturn(null);

        assertDoesNotThrow(() -> cacheHelper.clear("nonExistentCache"));
    }
}
