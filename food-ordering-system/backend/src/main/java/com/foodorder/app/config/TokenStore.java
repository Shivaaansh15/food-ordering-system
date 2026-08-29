package com.foodorder.app.config;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Minimal in-memory session-token store.
 *
 * NOTE: This is a lightweight stand-in for real authentication (e.g. Spring
 * Security + JWT) to keep the demo self-contained. Swap this out for
 * Spring Security with JWT filters in a production deployment.
 */
@Component
public class TokenStore {

    private final Map<String, Long> tokenToUserId = new ConcurrentHashMap<>();

    public String issueToken(Long userId) {
        String token = UUID.randomUUID().toString();
        tokenToUserId.put(token, userId);
        return token;
    }

    public Long resolve(String token) {
        return tokenToUserId.get(token);
    }

    public void revoke(String token) {
        tokenToUserId.remove(token);
    }
}
