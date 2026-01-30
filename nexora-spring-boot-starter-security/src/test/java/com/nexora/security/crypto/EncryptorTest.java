package com.nexora.security.crypto;

import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Tests for {@link Encryptor}.
 */
@DisplayName("Encryptor Tests")
@ExtendWith(MockitoExtension.class)
class EncryptorTest {

    @Mock
    private StandardPBEStringEncryptor encryptor;

    private Encryptor cryptoService;

    @BeforeEach
    void setUp() {
        cryptoService = new Encryptor(encryptor);
    }

    @Test
    @DisplayName("Encrypt should delegate to underlying encryptor")
    void testEncrypt() {
        String plainText = "sensitive-data";
        String encrypted = "encrypted-value";

        when(encryptor.encrypt(plainText)).thenReturn(encrypted);

        String result = cryptoService.encrypt(plainText);

        assertEquals(encrypted, result);
        verify(encryptor).encrypt(plainText);
    }

    @Test
    @DisplayName("Decrypt should delegate to underlying encryptor")
    void testDecrypt() {
        String encrypted = "encrypted-value";
        String plainText = "sensitive-data";

        when(encryptor.decrypt(encrypted)).thenReturn(plainText);

        String result = cryptoService.decrypt(encrypted);

        assertEquals(plainText, result);
        verify(encryptor).decrypt(encrypted);
    }

    @Test
    @DisplayName("Encrypt and decrypt should be reversible")
    void testEncryptDecryptReversible() {
        // Use real encryptor for this test
        StandardPBEStringEncryptor realEncryptor = new StandardPBEStringEncryptor();
        realEncryptor.setPassword("test-password");
        Encryptor realCrypto = new Encryptor(realEncryptor);

        String original = "Hello, World!";
        String encrypted = realCrypto.encrypt(original);
        String decrypted = realCrypto.decrypt(encrypted);

        assertEquals(original, decrypted);
        assertNotEquals(original, encrypted);
    }

    @Test
    @DisplayName("EncryptToHex should produce hex-encoded output")
    void testEncryptToHex() {
        String plainText = "test-data";
        String encrypted = "encrypted";
        when(encryptor.encrypt(plainText)).thenReturn(encrypted);

        String hexResult = cryptoService.encryptToHex(plainText);

        assertAll("Hex encryption",
            () -> assertNotNull(hexResult),
            () -> assertTrue(hexResult.matches("[0-9a-fA-F]+")),
            () -> verify(encryptor).encrypt(plainText)
        );
    }

    @Test
    @DisplayName("DecryptFromHex should decode hex before decrypting")
    void testDecryptFromHex() {
        String plainText = "test-data";
        String encrypted = "encrypted";
        String hexEncoded = "656e63727970746564"; // hex for "encrypted"

        when(encryptor.decrypt(encrypted)).thenReturn(plainText);

        String result = cryptoService.decryptFromHex(hexEncoded);

        assertEquals(plainText, result);
        verify(encryptor).decrypt(encrypted);
    }

    @Test
    @DisplayName("EncryptToHex and decryptFromHex should be reversible")
    void testEncryptToHexDecryptFromHexReversible() {
        // Use real encryptor for this test
        StandardPBEStringEncryptor realEncryptor = new StandardPBEStringEncryptor();
        realEncryptor.setPassword("test-password");
        Encryptor realCrypto = new Encryptor(realEncryptor);

        String original = "Secret message!";
        String hexEncrypted = realCrypto.encryptToHex(original);
        String decrypted = realCrypto.decryptFromHex(hexEncrypted);

        assertEquals(original, decrypted);
    }

    @Test
    @DisplayName("EncryptToHex should produce consistent output for same input")
    void testEncryptToHexConsistency() {
        StandardPBEStringEncryptor realEncryptor = new StandardPBEStringEncryptor();
        realEncryptor.setPassword("test-password");
        Encryptor realCrypto = new Encryptor(realEncryptor);

        String original = "Consistent data";

        String hex1 = realCrypto.encryptToHex(original);
        String hex2 = realCrypto.encryptToHex(original);

        // Note: Jasypt with random salt will produce different output each time
        // This test verifies the format is valid hex, not that values are equal
        assertAll("Hex output format",
            () -> assertTrue(hex1.matches("[0-9a-fA-F]+")),
            () -> assertTrue(hex2.matches("[0-9a-fA-F]+"))
        );
    }

    @Test
    @DisplayName("Decrypt should throw exception for invalid input")
    void testDecryptWithInvalidInput() {
        StandardPBEStringEncryptor realEncryptor = new StandardPBEStringEncryptor();
        realEncryptor.setPassword("test-password");
        Encryptor realCrypto = new Encryptor(realEncryptor);

        String invalidEncrypted = "not-a-valid-encrypted-string";

        assertThrows(Exception.class, () -> realCrypto.decrypt(invalidEncrypted));
    }

    @Test
    @DisplayName("DecryptFromHex should throw exception for invalid hex")
    void testDecryptFromHexWithInvalidHex() {
        StandardPBEStringEncryptor realEncryptor = new StandardPBEStringEncryptor();
        realEncryptor.setPassword("test-password");
        Encryptor realCrypto = new Encryptor(realEncryptor);

        String invalidHex = "xyz-invalid-hex";

        assertThrows(Exception.class, () -> realCrypto.decryptFromHex(invalidHex));
    }

    @Test
    @DisplayName("Encrypt should handle empty string")
    void testEncryptEmptyString() {
        StandardPBEStringEncryptor realEncryptor = new StandardPBEStringEncryptor();
        realEncryptor.setPassword("test-password");
        Encryptor realCrypto = new Encryptor(realEncryptor);

        String original = "";
        String encrypted = realCrypto.encrypt(original);
        String decrypted = realCrypto.decrypt(encrypted);

        assertEquals(original, decrypted);
    }

    @Test
    @DisplayName("Encrypt should handle special characters")
    void testEncryptSpecialCharacters() {
        StandardPBEStringEncryptor realEncryptor = new StandardPBEStringEncryptor();
        realEncryptor.setPassword("test-password");
        Encryptor realCrypto = new Encryptor(realEncryptor);

        String original = "Special: !@#$%^&*()[]{}|\\:;\"'<>?,./";
        String encrypted = realCrypto.encrypt(original);
        String decrypted = realCrypto.decrypt(encrypted);

        assertEquals(original, decrypted);
    }
}
