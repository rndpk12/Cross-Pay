package com.crosspay.auth.service;

import com.crosspay.user.entity.User;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JwtServiceTest {

    private static final String SECRET = "test-only-secret-must-be-at-least-32-bytes";
    private final JwtService jwtService = new JwtService(SECRET, 900_000);

    @Test
    void acceptsOnlyValidTokenWithUuidSubject() {
        UUID userId = UUID.randomUUID();
        User user = new User();
        user.setId(userId);
        user.setEmail("user@example.com");

        String token = jwtService.generateAccessToken(user);

        assertTrue(jwtService.isValid(token));
        assertEquals(userId, jwtService.extractUserId(token).orElseThrow());
    }

    @Test
    void rejectsMalformedExpiredInvalidSignatureAndInvalidSubjectTokens() {
        SecretKey key = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
        String expired = token(key, UUID.randomUUID().toString(), new Date(System.currentTimeMillis() - 1_000));
        String invalidSubject = token(key, "not-a-uuid", new Date(System.currentTimeMillis() + 60_000));
        SecretKey otherKey = Keys.hmacShaKeyFor("another-test-secret-must-be-at-least-32-bytes".getBytes(StandardCharsets.UTF_8));
        String invalidSignature = token(otherKey, UUID.randomUUID().toString(), new Date(System.currentTimeMillis() + 60_000));

        for (String token : new String[]{"not-a-jwt", expired, invalidSubject, invalidSignature}) {
            assertFalse(jwtService.isValid(token));
            assertTrue(jwtService.extractUserId(token).isEmpty());
        }
    }

    @Test
    void rejectsUnsafeJwtConfigurationAtStartup() {
        assertThrows(IllegalStateException.class, () -> new JwtService("too-short", 900_000));
        assertThrows(IllegalStateException.class, () -> new JwtService(SECRET, 0));
    }

    private String token(SecretKey key, String subject, Date expiration) {
        return Jwts.builder()
                .subject(subject)
                .issuedAt(new Date())
                .expiration(expiration)
                .signWith(key, Jwts.SIG.HS256)
                .compact();
    }
}
