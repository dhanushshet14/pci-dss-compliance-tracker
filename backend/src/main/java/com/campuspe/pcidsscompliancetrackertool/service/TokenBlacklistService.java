package com.campuspe.pcidsscompliancetrackertool.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

/**
 * BUG-5 FIX: JWT Token Blacklist Service.
 *
 * <p>When a user calls {@code POST /auth/logout} the current access token is
 * stored in Redis with a TTL equal to its remaining validity window.  Every
 * subsequent request is rejected by {@link com.campuspe.pcidsscompliancetrackertool.security.JwtAuthFilter}
 * if the presented token is found in this blacklist.</p>
 *
 * <p>Using Redis as the store means:
 * <ul>
 *   <li>The blacklist is shared across all application replicas (horizontal scaling).</li>
 *   <li>Entries expire automatically — no manual cleanup job needed.</li>
 *   <li>The JwtUtil signing key does NOT need to change on every logout.</li>
 * </ul>
 * </p>
 */
@Service
public class TokenBlacklistService {

    private static final Logger log = LoggerFactory.getLogger(TokenBlacklistService.class);

    /** Redis key prefix used for all blacklisted token entries. */
    private static final String BLACKLIST_PREFIX = "blacklist:token:";

    private final StringRedisTemplate redisTemplate;

    public TokenBlacklistService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * Adds a token to the blacklist for the given duration.
     *
     * @param token          the raw JWT string to blacklist
     * @param remainingTtl   how long the entry should live in Redis
     *                       (should match the token's remaining validity)
     */
    public void blacklist(String token, Duration remainingTtl) {
        String key = BLACKLIST_PREFIX + token;
        redisTemplate.opsForValue().set(key, "blacklisted", remainingTtl);
        log.debug("Token blacklisted (TTL={}s)", remainingTtl.getSeconds());
    }

    /**
     * Checks whether a token has been blacklisted.
     *
     * @param token the raw JWT string to check
     * @return {@code true} if the token is on the blacklist
     */
    public boolean isBlacklisted(String token) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(BLACKLIST_PREFIX + token));
    }
}
