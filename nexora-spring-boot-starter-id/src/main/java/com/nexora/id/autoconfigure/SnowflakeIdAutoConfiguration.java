package com.nexora.id.autoconfigure;

import com.nexora.id.SnowflakeIdGenerator;
import com.nexora.id.SnowflakeProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Snowflake ID generator auto-configuration.
 *
 * <p>Configuration properties (application.yml):
 * <pre>
 * snowflake:
 *   worker-id: 0        # Worker ID (0-31), unique per instance
 *   datacenter-id: 0    # Datacenter ID (0-31), unique per datacenter
 *   epoch: 1704067200000  # Epoch timestamp (default: 2024-01-01 00:00:00 UTC)
 * </pre>
 *
 * @author sujie
 */
@Configuration
@EnableConfigurationProperties(SnowflakeProperties.class)
@ConditionalOnMissingBean(SnowflakeIdGenerator.class)
public class SnowflakeIdAutoConfiguration {

    @Bean
    public SnowflakeIdGenerator snowflakeIdGenerator(SnowflakeProperties properties) {
        return new SnowflakeIdGenerator(
            properties.getEpoch(),
            properties.getWorkerId(),
            properties.getDatacenterId()
        );
    }
}
