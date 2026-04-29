package com.jpsoftware.farmapp.unit.auth;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.jpsoftware.farmapp.auth.infrastructure.JwtTokenService;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import org.junit.jupiter.api.Test;

class JwtTokenServiceTest {

    private static final String DEFAULT_SECRET =
            "0123456789abcdef0123456789abcdef0123456789abcdef0123456789abcdef";

    @Test
    void shouldRejectDefaultSecretWhenNotExplicitlyAllowed() {
        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> new JwtTokenService(DEFAULT_SECRET, false, 86_400_000L));

        org.junit.jupiter.api.Assertions.assertEquals(
                "Default JWT secret is not allowed outside local/test environments",
                exception.getMessage());
    }

    @Test
    void shouldAllowDefaultSecretInLocalOrTestMode() {
        JwtTokenService tokenService = new JwtTokenService(DEFAULT_SECRET, true, 86_400_000L);

        assertTrue(tokenService.validateToken(buildToken(DEFAULT_SECRET, "11111111-1111-1111-1111-111111111111")));
    }

    @Test
    void shouldRejectTokenWithNonUuidSubject() {
        String secret = "fedcba9876543210fedcba9876543210fedcba9876543210fedcba9876543210";
        JwtTokenService tokenService = new JwtTokenService(secret, false, 86_400_000L);

        assertFalse(tokenService.validateToken(buildToken(secret, "not-a-uuid")));
    }

    private String buildToken(String secret, String subject) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(subject)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(3600)))
                .signWith(Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8)))
                .compact();
    }
}
