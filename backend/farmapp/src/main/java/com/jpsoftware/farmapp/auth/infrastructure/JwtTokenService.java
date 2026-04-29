package com.jpsoftware.farmapp.auth.infrastructure;

import com.jpsoftware.farmapp.auth.service.TokenService;
import com.jpsoftware.farmapp.user.entity.UserEntity;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;
import javax.crypto.SecretKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class JwtTokenService implements TokenService {

    static final String DEFAULT_JWT_SECRET =
            "0123456789abcdef0123456789abcdef0123456789abcdef0123456789abcdef";

    private static final Logger logger = LoggerFactory.getLogger(JwtTokenService.class);

    private final SecretKey secretKey;
    private final long expirationInMillis;

    public JwtTokenService(
            @Value("${app.security.jwt.secret}") String secret,
            @Value("${app.security.jwt.allow-default-secret:false}") boolean allowDefaultSecret,
            @Value("${app.security.jwt.expiration}") long expirationInMillis) {
        validateSecret(secret, allowDefaultSecret);
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expirationInMillis = expirationInMillis;
    }

    @Override
    public String generateToken(UserEntity user) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(user.getId().toString())
                .claim("role", user.getRole())
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusMillis(expirationInMillis)))
                .signWith(secretKey)
                .compact();
    }

    @Override
    public boolean validateToken(String token) {
        try {
            Claims claims = parseClaims(token);
            UUID.fromString(claims.getSubject());
            return true;
        } catch (RuntimeException exception) {
            return false;
        }
    }

    @Override
    public UUID extractUserId(String token) {
        return UUID.fromString(parseClaims(token).getSubject());
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private void validateSecret(String secret, boolean allowDefaultSecret) {
        if (!StringUtils.hasText(secret)) {
            throw new IllegalStateException("app.security.jwt.secret must not be blank");
        }

        if (DEFAULT_JWT_SECRET.equals(secret)) {
            if (!allowDefaultSecret) {
                throw new IllegalStateException(
                        "Default JWT secret is not allowed outside local/test environments");
            }

            logger.warn("Using the default JWT secret. Restrict this to local/test environments only.");
        }
    }
}
