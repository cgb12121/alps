package me.mb.alps.infrastructure.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * JWT config: secret (min 256 bits for HS256), validity.
 */
@ConfigurationProperties(prefix = "alps.jwt")
public record JwtProperties(
        String secret,
        long validityMs
) {
    public static final long DEFAULT_VALIDITY_MS = 86400_000L; // 24h
}
