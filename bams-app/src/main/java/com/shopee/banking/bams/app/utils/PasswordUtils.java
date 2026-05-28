package com.shopee.banking.bams.app.utils;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;
import java.util.regex.Pattern;

public class PasswordUtils {

    private static final Pattern BCRYPT_PATTERN = Pattern.compile("^\\$2[aby]?\\$\\d{2}\\$.{53}$");
    private static final String SHA_256_PREFIX = "sha256:";
    private static final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();


    public static boolean passwordMatches(String rawPassword, String storedPassword) {
        if (!StringUtils.hasText(rawPassword) || !StringUtils.hasText(storedPassword)) {
            return false;
        }
        if (BCRYPT_PATTERN.matcher(storedPassword).matches()) {
            return passwordEncoder.matches(rawPassword, storedPassword);
        }

        // Non-BCrypt seed data is assumed to store a SHA-256 hex digest, optionally prefixed with "sha256:".
        String normalizedStoredPassword = storedPassword.trim().toLowerCase(Locale.ROOT);
        if (normalizedStoredPassword.startsWith(SHA_256_PREFIX)) {
            normalizedStoredPassword = normalizedStoredPassword.substring(SHA_256_PREFIX.length());
        }
        return MessageDigest.isEqual(
                sha256Hex(rawPassword).getBytes(StandardCharsets.UTF_8),
                normalizedStoredPassword.getBytes(StandardCharsets.UTF_8)
        );
    }

    private static String sha256Hex(String value) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder(digest.length * 2);
            for (byte currentByte : digest) {
                hex.append(String.format("%02x", currentByte));
            }
            return hex.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is not available", e);
        }
    }
}
