package com.nexora.security.autoconfigure;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

/**
 * Security properties for encryption and JWT.
 *
 * <p>Configuration example:
 * <pre>
 * common.security.jasypt.enabled=true
 * common.security.jasypt.password=${JASYPT_PASSWORD}
 * common.security.jwt.secret=${JWT_SECRET}
 * common.security.jwt.expiration=1h
 * </pre>
 *
 * @author sujie
 */
@Data
@ConfigurationProperties(prefix = "common.security")
public class SecurityProperties {

    /**
     * Jasypt encryption configuration.
     */
    private Jasypt jasypt = new Jasypt();

    /**
     * JWT configuration.
     */
    private Jwt jwt = new Jwt();

    @Data
    public static class Jasypt {
        /**
         * Enable Jasypt encryption.
         */
        private boolean enabled = false;

        /**
         * Encryption password (should be externalized).
         */
        private String password;

        /**
         * Encryption algorithm.
         */
        private String algorithm = "PBEWITHHMACSHA512ANDAES_256";

        /**
         * Key obtention iterations.
         */
        private int keyObtentionIterations = 1000;

        /**
         * Pool size.
         */
        private int poolSize = 1;

        /**
         * Salt generator class name.
         */
        private String saltGeneratorClassname = "org.jasypt.salt.RandomSaltGenerator";

        /**
         * IV generator class name.
         */
        private String ivGeneratorClassname = "org.jasypt.iv.RandomIvGenerator";
    }

    @Data
    public static class Jwt {
        /**
         * Enable JWT support.
         */
        private boolean enabled = false;

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
}
