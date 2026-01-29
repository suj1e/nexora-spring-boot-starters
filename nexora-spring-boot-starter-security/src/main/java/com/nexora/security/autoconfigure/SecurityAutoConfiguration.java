package com.nexora.security.autoconfigure;

import com.nexora.security.crypto.Encryptor;
import com.nexora.security.jwt.JwtProperties;
import com.nexora.security.jwt.JwtTokenProvider;
import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Security auto-configuration.
 *
 * <p>Registers JWT and encryption beans.
 *
 * @author sujie
 */
@Configuration
@ConditionalOnClass(StandardPBEStringEncryptor.class)
public class SecurityAutoConfiguration {

    /**
     * JWT Token Provider configuration.
     */
    @Configuration
    @EnableConfigurationProperties(JwtProperties.class)
    @ConditionalOnProperty(prefix = "nexora.security.jwt", name = "enabled", havingValue = "true")
    public static class JwtTokenProviderConfiguration {

        @Bean
        @ConditionalOnMissingBean
        public JwtTokenProvider jwtTokenProvider(JwtProperties properties) {
            return new JwtTokenProvider(properties);
        }
    }

    /**
     * Encryptor configuration.
     */
    @Configuration
    @ConditionalOnClass(StandardPBEStringEncryptor.class)
    public static class EncryptorConfiguration {

        @Bean
        @ConditionalOnMissingBean
        public Encryptor encryptor(StandardPBEStringEncryptor encryptor) {
            return new Encryptor(encryptor);
        }
    }
}
