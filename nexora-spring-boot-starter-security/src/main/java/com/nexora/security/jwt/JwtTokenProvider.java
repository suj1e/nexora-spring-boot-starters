package com.nexora.security.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.Map;

/**
 * JWT token provider for generating and validating tokens.
 *
 * <p>Usage:
 * <pre>
 * &#64;Autowired
 * private JwtTokenProvider tokenProvider;
 *
 * // Generate token
 * String token = tokenProvider.generateToken(userId, username, claims);
 *
 * // Validate token
 * if (tokenProvider.validateToken(token)) {
 *     Claims claims = tokenProvider.getClaims(token);
 * }
 * </pre>
 *
 * @author sujie
 */
@Slf4j
@Component
@EnableConfigurationProperties(JwtProperties.class)
@ConditionalOnProperty(prefix = "nexora.security.jwt", name = "enabled", havingValue = "true")
public class JwtTokenProvider {

    private final JwtProperties properties;
    private SecretKey secretKey;

    public JwtTokenProvider(JwtProperties properties) {
        this.properties = properties;
    }

    @PostConstruct
    public void init() {
        if (properties.getSecret() == null || properties.getSecret().isEmpty()) {
            throw new IllegalArgumentException("JWT secret must not be empty");
        }

        this.secretKey = Keys.hmacShaKeyFor(properties.getSecret().getBytes(StandardCharsets.UTF_8));
        log.info("Initialized JwtTokenProvider with issuer: {}", properties.getIssuer());
    }

    /**
     * Generate JWT token.
     *
     * @param subject the subject (usually user ID)
     * @param claims  additional claims
     * @return the JWT token
     */
    public String generateToken(String subject, Map<String, Object> claims) {
        Instant now = Instant.now();
        Instant expiry = now.plus(properties.getExpiration());

        JwtBuilder builder = Jwts.builder()
                .subject(subject)
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiry))
                .issuer(properties.getIssuer())
                .audience().add(properties.getAudience()).and()
                .signWith(secretKey);

        if (claims != null && !claims.isEmpty()) {
            builder.claims(claims);
        }

        return builder.compact();
    }

    /**
     * Generate refresh token.
     *
     * @param subject the subject (usually user ID)
     * @return the refresh token
     */
    public String generateRefreshToken(String subject) {
        Instant now = Instant.now();
        Instant expiry = now.plus(properties.getRefreshExpiration());

        return Jwts.builder()
                .subject(subject)
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiry))
                .issuer(properties.getIssuer())
                .signWith(secretKey)
                .compact();
    }

    /**
     * Validate JWT token.
     *
     * @param token the token to validate
     * @return true if valid, false otherwise
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            log.warn("Invalid JWT token: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Get claims from token.
     *
     * @param token the JWT token
     * @return the claims
     */
    public Claims getClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * Get subject from token.
     *
     * @param token the JWT token
     * @return the subject
     */
    public String getSubject(String token) {
        return getClaims(token).getSubject();
    }

    /**
     * Get expiration from token.
     *
     * @param token the JWT token
     * @return the expiration date
     */
    public Date getExpiration(String token) {
        return getClaims(token).getExpiration();
    }

    /**
     * Check if token is expired.
     *
     * @param token the JWT token
     * @return true if expired, false otherwise
     */
    public boolean isTokenExpired(String token) {
        try {
            Date expiration = getExpiration(token);
            return expiration.before(new Date());
        } catch (Exception e) {
            return true;
        }
    }

    /**
     * Extract username from token.
     *
     * @param token the JWT token
     * @return the username
     */
    public String getUsername(String token) {
        return getClaims(token).get("username", String.class);
    }
}
