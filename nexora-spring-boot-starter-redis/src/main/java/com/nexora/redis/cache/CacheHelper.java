package com.nexora.redis.cache;

import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

import java.util.Optional;
import java.util.function.Supplier;

/**
 * Cache helper utility.
 *
 * <p>Provides convenient methods for cache operations with fallback logic.
 *
 * @author sujie
 */
public class CacheHelper {

    private final CacheManager cacheManager;

    public CacheHelper(CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    /**
     * Get value from cache, or compute and cache if not present.
     *
     * @param cacheName the cache name
     * @param key       the cache key
     * @param loader    the value loader
     * @param <T>       the value type
     * @return the cached or computed value
     */
    @SuppressWarnings("unchecked")
    public <T> T getOrCompute(String cacheName, Object key, Supplier<T> loader) {
        Cache cache = cacheManager.getCache(cacheName);
        if (cache == null) {
            return loader.get();
        }

        Cache.ValueWrapper wrapper = cache.get(key);
        if (wrapper != null) {
            return (T) wrapper.get();
        }

        T value = loader.get();
        cache.put(key, value);
        return value;
    }

    /**
     * Get value from cache as Optional.
     *
     * @param cacheName the cache name
     * @param key       the cache key
     * @param <T>       the value type
     * @return Optional containing the value, or empty if not found
     */
    @SuppressWarnings("unchecked")
    public <T> Optional<T> getOptional(String cacheName, Object key) {
        Cache cache = cacheManager.getCache(cacheName);
        if (cache == null) {
            return Optional.empty();
        }

        Cache.ValueWrapper wrapper = cache.get(key);
        return wrapper != null
                ? Optional.ofNullable((T) wrapper.get())
                : Optional.empty();
    }

    /**
     * Evict entry from cache.
     *
     * @param cacheName the cache name
     * @param key       the cache key
     */
    public void evict(String cacheName, Object key) {
        Cache cache = cacheManager.getCache(cacheName);
        if (cache != null) {
            cache.evict(key);
        }
    }

    /**
     * Clear all entries in cache.
     *
     * @param cacheName the cache name
     */
    public void clear(String cacheName) {
        Cache cache = cacheManager.getCache(cacheName);
        if (cache != null) {
            cache.clear();
        }
    }
}
