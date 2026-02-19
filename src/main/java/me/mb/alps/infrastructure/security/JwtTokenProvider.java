package me.mb.alps.infrastructure.security;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.List;

/**
 * Create and validate JWT (HS256) using Nimbus. Secret from alps.jwt.secret (min 32 bytes for HS256).
 */
@Slf4j
@Component
@EnableConfigurationProperties(JwtProperties.class)
public class JwtTokenProvider {

    private final JwtProperties properties;
    private final JWSSigner signer;
    private final JWSVerifier verifier;

    public JwtTokenProvider(JwtProperties properties) {
        this.properties = properties;
        byte[] secretBytes = properties.secret().getBytes(StandardCharsets.UTF_8);
        if (secretBytes.length < 32) {
            throw new IllegalArgumentException("alps.jwt.secret must be at least 32 bytes for HS256");
        }
        try {
            this.signer = new MACSigner(secretBytes);
            this.verifier = new MACVerifier(secretBytes);
        } catch (JOSEException e) {
            throw new RuntimeException("JWT signer/verifier init failed", e);
        }
    }

    public String createToken(String username, String userId, List<String> roles) {
        try {
            Instant now = Instant.now();
            Instant exp = now.plusMillis(properties.validityMs());
            JWTClaimsSet claims = new JWTClaimsSet.Builder()
                    .subject(username)
                    .claim("userId", userId)
                    .claim("roles", roles)
                    .issueTime(Date.from(now))
                    .expirationTime(Date.from(exp))
                    .build();
            SignedJWT signedJWT = new SignedJWT(new JWSHeader(JWSAlgorithm.HS256), claims);
            signedJWT.sign(signer);
            return signedJWT.serialize();
        } catch (Exception e) {
            log.error("Failed to generate token for loan {}: {}", username, e.getMessage());
            throw new RuntimeException("Failed to create JWT");
        }
    }

    /** Returns subject (username) or null if invalid. */
    public String validateAndGetSubject(String token) {
        JWTClaimsSet claims = validateAndGetClaims(token);
        return claims != null ? claims.getSubject() : null;
    }

    /** Returns claims or null if invalid. Use for roles. */
    public JWTClaimsSet validateAndGetClaims(String token) {
        try {
            SignedJWT signed = SignedJWT.parse(token);
            if (!signed.verify(verifier)) {
                log.error("JWT verification failed");
                throw new SecurityException("JWT verification failed");
            }
            JWTClaimsSet claims = signed.getJWTClaimsSet();
            if (claims.getExpirationTime() == null || claims.getExpirationTime().before(new Date())) {
                log.warn("JWT expired");
                throw new SecurityException("JWT verification failed");
            }
            return claims;
        } catch (Exception e) {
            log.error("Failed to validate JWT", e);
            throw new SecurityException("Failed to validate token");
        }
    }
}
