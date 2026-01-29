package com.nexora.security.jwt;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

/**
 * JWT specific properties.
 *
 * <p>This is a convenience class for JWT configuration.
 * For full configuration, use SecurityProperties.
 *
 * @author sujie
 */
@Data
@ConfigurationProperties(prefix = "nexora.security.jwt")
public class JwtProperties {

    /**
     * JWT secret key (should be at least 256 bits).
     */
    private String secret;

    /**
     * Token expiration time.
     */
    private Duration expiration = Duration.ofHours(1);

    /**
     * Refresh token expiration time.
     */
    private Duration refreshExpiration = Duration.ofDays(7);

    /**
     * Token issuer.
     */
    private String issuer = "nexora-auth";

    /**
     * Token audience.
     */
    private String audience = "nexora-api";
}
