package com.campuspe.pcidsscompliancetrackertool.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;
import java.time.Duration;
import java.time.Instant;

/**
 * Utility class for creating and validating JSON Web Tokens (JWT).
 *
 * <p>All configuration values (secret, expiry durations) are injected from
 * {@code application.yml} — nothing is hardcoded.</p>
 */
@Component
public class JwtUtil {

    /** HMAC-SHA signing key derived from the configured secret. */
    private final SecretKey signingKey;

    /** Access-token lifetime in milliseconds. */
    private final long accessExpirationMs;

    /** Refresh-token lifetime in milliseconds. */
    private final long refreshExpirationMs;

    /**
     * Constructs the utility with values injected from {@code application.yml}.
     *
     * @param secret               JWT signing secret ({@code jwt.secret})
     * @param expirySeconds        access-token lifetime in seconds ({@code jwt.expiry-seconds})
     * @param refreshExpirySeconds refresh-token lifetime in seconds ({@code jwt.refresh-expiry-seconds})
     */
    public JwtUtil(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.expiry-seconds}") long expirySeconds,
            @Value("${jwt.refresh-expiry-seconds}") long refreshExpirySeconds) {

        this.signingKey          = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.accessExpirationMs  = expirySeconds * 1000;
        this.refreshExpirationMs = refreshExpirySeconds * 1000;
    }

    // ── Token generation ──────────────────────────────────────────────────────

    /**
     * Generates a short-lived JWT access token.
     *
     * @param username the subject (username) to embed in the token
     * @param roles    list of role names to store in the {@code roles} claim
     * @return signed JWT string
     */
    public String generateToken(String username, List<String> roles) {
        return buildToken(username, roles, accessExpirationMs);
    }

    /**
     * Generates a long-lived JWT refresh token.
     *
     * @param username the subject (username) to embed in the token
     * @param roles    list of role names to store in the {@code roles} claim
     * @return signed JWT string
     */
    public String generateRefreshToken(String username, List<String> roles) {
        return buildToken(username, roles, refreshExpirationMs);
    }

    // ── Token validation ──────────────────────────────────────────────────────

    /**
     * Validates a JWT token — checks signature, expiration, and structure.
     *
     * @param token the JWT string to validate
     * @return {@code true} if the token is valid and not expired
     */
    public boolean validateToken(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    // ── Claim extraction ──────────────────────────────────────────────────────

    /**
     * Returns the remaining validity window of the token as a {@link Duration}.
     * Used by the logout flow to set the Redis TTL on the blacklisted token entry.
     *
     * @param token signed JWT string
     * @return positive duration if the token is still valid; {@link Duration#ZERO} if expired
     */
    public Duration extractRemainingTtl(String token) {
        Date expiry = parseClaims(token).getExpiration();
        Duration remaining = Duration.between(Instant.now(), expiry.toInstant());
        return remaining.isNegative() ? Duration.ZERO : remaining;
    }

    /**
     * Extracts the username (subject) from a valid JWT.
     *
     * @param token signed JWT string
     * @return the username embedded as the token subject
     */
    public String extractUsername(String token) {
        return parseClaims(token).getSubject();
    }

    /**
     * Extracts the roles claim from a valid JWT.
     *
     * @param token signed JWT string
     * @return list of role strings
     */
    @SuppressWarnings("unchecked")
    public List<String> extractRoles(String token) {
        return parseClaims(token).get("roles", List.class);
    }

    // ── Internal helpers ──────────────────────────────────────────────────────

    /**
     * Builds a signed JWT with the given subject, roles, and expiration.
     */
    private String buildToken(String username, List<String> roles, long expirationMs) {
        Date now    = new Date();
        Date expiry = new Date(now.getTime() + expirationMs);

        return Jwts.builder()
                .subject(username)
                .claim("roles", roles)
                .issuedAt(now)
                .expiration(expiry)
                .signWith(signingKey)
                .compact();
    }

    /**
     * Parses and verifies a JWT, returning the claims body.
     *
     * @throws JwtException if the token is malformed, expired, or has an invalid signature
     */
    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
