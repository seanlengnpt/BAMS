package com.shopee.banking.bams.common.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.Date;

public final class JwtUtils {
    public static final String ROLE_ADMIN = "admin";
    public static final String ROLE_CLAIM = "role";
    public static final String USERNAME_CLAIM = "username";


    private JwtUtils() {
    }

    public static String createAccessToken(Long adminId,
                                           String username,
                                           String secret,
                                           long ttlSeconds) {
        return createToken(String.valueOf(adminId), username, secret, ttlSeconds);
    }

    public static String createRefreshToken(Long adminId,
                                            String username,
                                            String secret,
                                            long ttlSeconds) {
        return createToken(String.valueOf(adminId), username, secret, ttlSeconds);
    }

    public static Claims parseClaims(String token, String secret) {
        return Jwts.parser()
                .verifyWith(signingKey(secret))
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }


    private static String createToken(String subject,
                                      String username,
                                      String secret,
                                      long ttlSeconds) {
        Instant now = Instant.now();
        Instant expiresAt = now.plusSeconds(ttlSeconds);
        return Jwts.builder()
                .subject(subject)
                .claim(ROLE_CLAIM, ROLE_ADMIN)
                .claim(USERNAME_CLAIM, username)
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiresAt))
                .signWith(signingKey(secret))
                .compact();
    }

    private static SecretKey signingKey(String secret) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(secret.getBytes(StandardCharsets.UTF_8));
            return Keys.hmacShaKeyFor(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is not available", e);
        }
    }
}
