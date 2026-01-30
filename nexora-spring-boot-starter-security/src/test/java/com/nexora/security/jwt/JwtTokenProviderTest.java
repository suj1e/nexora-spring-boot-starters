package com.nexora.security.jwt;

import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link JwtTokenProvider}.
 */
@DisplayName("JwtTokenProvider Tests")
class JwtTokenProviderTest {

    private JwtTokenProvider tokenProvider;
    private JwtProperties properties;

    @BeforeEach
    void setUp() {
        properties = new JwtProperties();
        properties.setSecret("a".repeat(64)); // 64 chars for HS256
        properties.setExpiration(Duration.ofHours(1));
        properties.setRefreshExpiration(Duration.ofDays(7));
        properties.setIssuer("test-issuer");
        properties.setAudience("test-audience");

        tokenProvider = new JwtTokenProvider(properties);
        tokenProvider.init();
    }

    @Test
    @DisplayName("Init should throw exception when secret is empty")
    void testInitThrowsExceptionForEmptySecret() {
        JwtProperties invalidProperties = new JwtProperties();
        invalidProperties.setSecret("");
        invalidProperties.setExpiration(Duration.ofHours(1));
        invalidProperties.setRefreshExpiration(Duration.ofDays(7));
        invalidProperties.setIssuer("test");
        invalidProperties.setAudience("test");

        JwtTokenProvider invalidProvider = new JwtTokenProvider(invalidProperties);

        assertThrows(IllegalArgumentException.class, invalidProvider::init);
    }

    @Test
    @DisplayName("Init should throw exception when secret is null")
    void testInitThrowsExceptionForNullSecret() {
        JwtProperties invalidProperties = new JwtProperties();
        invalidProperties.setSecret(null);
        invalidProperties.setExpiration(Duration.ofHours(1));
        invalidProperties.setRefreshExpiration(Duration.ofDays(7));
        invalidProperties.setIssuer("test");
        invalidProperties.setAudience("test");

        JwtTokenProvider invalidProvider = new JwtTokenProvider(invalidProperties);

        assertThrows(IllegalArgumentException.class, invalidProvider::init);
    }

    @Test
    @DisplayName("Generate token should create valid JWT")
    void testGenerateToken() {
        String subject = "user123";
        Map<String, Object> claims = new HashMap<>();
        claims.put("username", "testuser");
        claims.put("role", "ADMIN");

        String token = tokenProvider.generateToken(subject, claims);

        assertNotNull(token);
        assertFalse(token.isEmpty());
    }

    @Test
    @DisplayName("Validate token should return true for valid token")
    void testValidateValidToken() {
        String subject = "user123";
        Map<String, Object> claims = new HashMap<>();
        claims.put("username", "testuser");

        String token = tokenProvider.generateToken(subject, claims);

        assertTrue(tokenProvider.validateToken(token));
    }

    @Test
    @DisplayName("Validate token should return false for invalid token")
    void testValidateInvalidToken() {
        String invalidToken = "invalid.token.string";

        assertFalse(tokenProvider.validateToken(invalidToken));
    }

    @Test
    @DisplayName("Validate token should return false for malformed token")
    void testValidateMalformedToken() {
        String malformedToken = "eyJhbGciOiJIUzI1NiJ9.invalid";

        assertFalse(tokenProvider.validateToken(malformedToken));
    }

    @Test
    @DisplayName("Get claims should return correct claims")
    void testGetClaims() {
        String subject = "user123";
        Map<String, Object> claims = new HashMap<>();
        claims.put("username", "testuser");
        claims.put("email", "test@example.com");

        String token = tokenProvider.generateToken(subject, claims);
        io.jsonwebtoken.Claims parsedClaims = tokenProvider.getClaims(token);

        assertAll("Parsed claims",
            () -> assertEquals(subject, parsedClaims.getSubject()),
            () -> assertEquals("testuser", parsedClaims.get("username")),
            () -> assertEquals("test@example.com", parsedClaims.get("email"))
        );
    }

    @Test
    @DisplayName("Get subject should return correct subject")
    void testGetSubject() {
        String subject = "user456";
        String token = tokenProvider.generateToken(subject, null);

        String extractedSubject = tokenProvider.getSubject(token);

        assertEquals(subject, extractedSubject);
    }

    @Test
    @DisplayName("Get expiration should return valid expiration date")
    void testGetExpiration() {
        String subject = "user789";
        String token = tokenProvider.generateToken(subject, null);

        Date expiration = tokenProvider.getExpiration(token);

        assertNotNull(expiration);
        assertTrue(expiration.after(new Date()));
    }

    @Test
    @DisplayName("Is token expired should return false for fresh token")
    void testIsTokenExpiredForFreshToken() {
        String token = tokenProvider.generateToken("user", null);

        assertFalse(tokenProvider.isTokenExpired(token));
    }

    @Test
    @DisplayName("Generate refresh token should create valid token")
    void testGenerateRefreshToken() {
        String subject = "user123";

        String refreshToken = tokenProvider.generateRefreshToken(subject);

        assertNotNull(refreshToken);
        assertFalse(refreshToken.isEmpty());
        assertTrue(tokenProvider.validateToken(refreshToken));
    }

    @Test
    @DisplayName("Get username should extract username from claims")
    void testGetUsername() {
        String subject = "user123";
        Map<String, Object> claims = new HashMap<>();
        claims.put("username", "testuser");

        String token = tokenProvider.generateToken(subject, claims);
        String username = tokenProvider.getUsername(token);

        assertEquals("testuser", username);
    }

    @Test
    @DisplayName("Get username should return null when username not in claims")
    void testGetUsernameWhenNotInClaims() {
        String token = tokenProvider.generateToken("user", null);

        String username = tokenProvider.getUsername(token);

        assertNull(username);
    }

    @Test
    @DisplayName("Generate token with null claims should still work")
    void testGenerateTokenWithNullClaims() {
        String subject = "user123";

        String token = tokenProvider.generateToken(subject, null);

        assertNotNull(token);
        assertTrue(tokenProvider.validateToken(token));
        assertEquals(subject, tokenProvider.getSubject(token));
    }

    @Test
    @DisplayName("Generate token with empty claims should still work")
    void testGenerateTokenWithEmptyClaims() {
        String subject = "user123";
        Map<String, Object> emptyClaims = new HashMap<>();

        String token = tokenProvider.generateToken(subject, emptyClaims);

        assertNotNull(token);
        assertTrue(tokenProvider.validateToken(token));
    }

    @Test
    @DisplayName("Access token and refresh token should have different expirations")
    void testAccessAndRefreshTokenDifferentExpiration() {
        String subject = "user123";

        String accessToken = tokenProvider.generateToken(subject, null);
        String refreshToken = tokenProvider.generateRefreshToken(subject);

        Date accessExpiration = tokenProvider.getExpiration(accessToken);
        Date refreshExpiration = tokenProvider.getExpiration(refreshToken);

        assertTrue(refreshExpiration.after(accessExpiration),
            "Refresh token should expire later than access token");
    }
}
