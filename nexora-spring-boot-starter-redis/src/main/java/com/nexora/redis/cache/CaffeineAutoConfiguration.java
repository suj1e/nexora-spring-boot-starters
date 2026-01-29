package com.nexora.redis.autoconfigure;

import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

/**
 * Caffeine local cache auto-configuration.
 *
 * <p>Features:
 * <ul>
 *   <li>High-performance in-memory caching</li>
 *   <li>Configurable size and TTL</li>
 *   <li>Automatic eviction based on LRU</li>
 * </ul>
 *
 * @author sujie
 */
@Slf4j
@Configuration
@ConditionalOnClass(Caffeine.class)
@EnableCaching
@EnableConfigurationProperties(RedisProperties.class)
@ConditionalOnProperty(prefix = "nexora.redis", name = "enable-caffeine", havingValue = "true", matchIfMissing = true)
public class CaffeineAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public CaffeineCacheManager caffeineCacheManager(RedisProperties properties) {
        log.info("Initializing CaffeineCacheManager with spec: {}", properties.getCaffeineSpec());

        CaffeineCacheManager cacheManager = new CaffeineCacheManager();

        // Parse Caffeine spec: maximumSize=1000,expireAfterWrite=5m
        Caffeine<Object, Object> caffeineBuilder = Caffeine.newBuilder();

        String[] parts = properties.getCaffeineSpec().split(",");
        for (String part : parts) {
            String[] kv = part.split("=");
            if (kv.length == 2) {
                String key = kv[0].trim();
                String value = kv[1].trim();

                switch (key) {
                    case "maximumSize":
                        caffeineBuilder.maximumSize(Long.parseLong(value));
                        break;
                    case "expireAfterWrite":
                        caffeineBuilder.expireAfterWrite(parseDuration(value), TimeUnit.MILLISECONDS);
                        break;
                    case "expireAfterAccess":
                        caffeineBuilder.expireAfterAccess(parseDuration(value), TimeUnit.MILLISECONDS);
                        break;
                }
            }
        }

        cacheManager.setCaffeine(caffeineBuilder);
        return cacheManager;
    }

    private long parseDuration(String duration) {
        duration = duration.toLowerCase();
        if (duration.endsWith("ms")) {
            return Long.parseLong(duration.substring(0, duration.length() - 2));
        } else if (duration.endsWith("s")) {
            return Long.parseLong(duration.substring(0, duration.length() - 1)) * 1000;
        } else if (duration.endsWith("m")) {
            return Long.parseLong(duration.substring(0, duration.length() - 1)) * 60 * 1000;
        } else if (duration.endsWith("h")) {
            return Long.parseLong(duration.substring(0, duration.length() - 1)) * 60 * 60 * 1000;
        }
        return Long.parseLong(duration);
    }
}
