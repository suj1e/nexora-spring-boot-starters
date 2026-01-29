package com.nexora.security.crypto;

import lombok.extern.slf4j.Slf4j;
import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;
import org.springframework.stereotype.Component;

import java.util.HexFormat;

/**
 * Simple encryption utility using Jasypt.
 *
 * <p>Usage:
 * <pre>
 * &#64;Autowired
 * private Encryptor encryptor;
 *
 * // Encrypt
 * String encrypted = encryptor.encrypt("sensitive-data");
 *
 * // Decrypt
 * String decrypted = encryptor.decrypt(encrypted);
 * </pre>
 *
 * @author sujie
 */
@Slf4j
@Component
public class Encryptor {

    private final StandardPBEStringEncryptor encryptor;

    public Encryptor(StandardPBEStringEncryptor encryptor) {
        this.encryptor = encryptor;
    }

    /**
     * Encrypt a string.
     *
     * @param plainText the plain text to encrypt
     * @return the encrypted text
     */
    public String encrypt(String plainText) {
        return encryptor.encrypt(plainText);
    }

    /**
     * Decrypt a string.
     *
     * @param encryptedText the encrypted text
     * @return the decrypted plain text
     */
    public String decrypt(String encryptedText) {
        return encryptor.decrypt(encryptedText);
    }

    /**
     * Encrypt to hex string.
     *
     * @param plainText the plain text
     * @return hex encoded encrypted text
     */
    public String encryptToHex(String plainText) {
        byte[] encrypted = encryptor.encrypt(plainText).getBytes();
        return HexFormat.of().formatHex(encrypted);
    }

    /**
     * Decrypt from hex string.
     *
     * @param hexEncoded the hex encoded encrypted text
     * @return the decrypted plain text
     */
    public String decryptFromHex(String hexEncoded) {
        byte[] encrypted = HexFormat.of().parseHex(hexEncoded);
        return encryptor.decrypt(new String(encrypted));
    }
}
