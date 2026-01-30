package com.nexora.security.sms;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link SmsAuthenticationToken}.
 */
@DisplayName("SmsAuthenticationToken Tests")
class SmsAuthenticationTokenTest {

    @Test
    @DisplayName("Unauthenticated token should hold phone and SMS code")
    void testUnauthenticatedToken() {
        String phone = "+1234567890";
        String smsCode = "123456";

        SmsAuthenticationToken token = new SmsAuthenticationToken(phone, smsCode);

        assertAll("Unauthenticated token",
            () -> assertEquals(phone, token.getPrincipal()),
            () -> assertEquals(smsCode, token.getCredentials()),
            () -> assertEquals(phone, token.getPhone()),
            () -> assertEquals(smsCode, token.getSmsCode()),
            () -> assertFalse(token.isAuthenticated()),
            () -> assertTrue(token.getAuthorities().isEmpty())
        );
    }

    @Test
    @DisplayName("Authenticated token should hold principal and authorities")
    void testAuthenticatedToken() {
        Object principal = "user123";
        Object credentials = null;
        Collection<? extends GrantedAuthority> authorities = List.of(
            new SimpleGrantedAuthority("ROLE_USER"),
            new SimpleGrantedAuthority("ROLE_ADMIN")
        );

        SmsAuthenticationToken token = new SmsAuthenticationToken(principal, credentials, authorities);

        assertAll("Authenticated token",
            () -> assertEquals(principal, token.getPrincipal()),
            () -> assertEquals(credentials, token.getCredentials()),
            () -> assertEquals(principal, token.getPhone()),
            () -> assertEquals(2, token.getAuthorities().size()),
            () -> assertTrue(token.isAuthenticated())
        );
    }

    @Test
    @DisplayName("GetPhone should return null when principal is null")
    void testGetPhoneWithNullPrincipal() {
        SmsAuthenticationToken token = new SmsAuthenticationToken(null, "123456");

        assertNull(token.getPhone());
    }

    @Test
    @DisplayName("GetSmsCode should return null when credentials is null")
    void testGetSmsCodeWithNullCredentials() {
        SmsAuthenticationToken token = new SmsAuthenticationToken("phone", null);

        assertNull(token.getSmsCode());
    }

    @Test
    @DisplayName("GetPhone should convert principal to string")
    void testGetPhoneConvertsToString() {
        SmsAuthenticationToken token = new SmsAuthenticationToken(12345, "123456");

        assertEquals("12345", token.getPhone());
    }

    @Test
    @DisplayName("GetSmsCode should convert credentials to string")
    void testGetSmsCodeConvertsToString() {
        SmsAuthenticationToken token = new SmsAuthenticationToken("phone", 123456);

        assertEquals("123456", token.getSmsCode());
    }

    @Test
    @DisplayName("Empty authorities collection should be valid")
    void testEmptyAuthorities() {
        Collection<GrantedAuthority> emptyAuthorities = List.of();

        SmsAuthenticationToken token = new SmsAuthenticationToken("user", null, emptyAuthorities);

        assertAll("Token with empty authorities",
            () -> assertTrue(token.getAuthorities().isEmpty()),
            () -> assertTrue(token.isAuthenticated())
        );
    }

    @Test
    @DisplayName("Authenticated token should contain correct authorities")
    void testAuthoritiesContent() {
        Collection<? extends GrantedAuthority> authorities = List.of(
            new SimpleGrantedAuthority("ROLE_USER"),
            new SimpleGrantedAuthority("ROLE_ADMIN")
        );

        SmsAuthenticationToken token = new SmsAuthenticationToken("user", null, authorities);

        assertTrue(token.getAuthorities().stream()
            .anyMatch(auth -> auth.getAuthority().equals("ROLE_USER")));
        assertTrue(token.getAuthorities().stream()
            .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN")));
    }

    @Test
    @DisplayName("Token should extend UsernamePasswordAuthenticationToken")
    void testTokenType() {
        SmsAuthenticationToken token = new SmsAuthenticationToken("phone", "code");

        assertInstanceOf(org.springframework.security.authentication.UsernamePasswordAuthenticationToken.class, token);
    }

    @Test
    @DisplayName("Two authenticated tokens with same data should be equal")
    void testTokenEquality() {
        Collection<? extends GrantedAuthority> authorities = List.of(
            new SimpleGrantedAuthority("ROLE_USER")
        );

        SmsAuthenticationToken token1 = new SmsAuthenticationToken("user", null, authorities);
        SmsAuthenticationToken token2 = new SmsAuthenticationToken("user", null, authorities);

        assertEquals(token1, token2);
        assertEquals(token1.hashCode(), token2.hashCode());
    }

    @Test
    @DisplayName("Two unauthenticated tokens with same data should be equal")
    void testUnauthenticatedTokenEquality() {
        SmsAuthenticationToken token1 = new SmsAuthenticationToken("phone", "code");
        SmsAuthenticationToken token2 = new SmsAuthenticationToken("phone", "code");

        assertEquals(token1, token2);
        assertEquals(token1.hashCode(), token2.hashCode());
    }
}
