package com.tanalytics.privacy.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class JwtService {

    private final SecretKey signingKey;
    private final String internalAudience;

    public JwtService(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.internal-audience:auth-internal}") String internalAudience
    ) {
        this.signingKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.internalAudience = internalAudience;
    }

    public Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public String extractSubject(String token) {
        return parseToken(token).getSubject();
    }

    public Optional<UUID> extractUserId(String token) {
        try {
            return Optional.of(UUID.fromString(extractSubject(token)));
        } catch (Exception ex) {
            return Optional.empty();
        }
    }

    public Optional<String> extractRole(String token) {
        return Optional.ofNullable(parseToken(token).get("role", String.class));
    }

    public List<String> extractSiteIds(String token) {
        Object siteIds = parseToken(token).get("siteIds");
        if (siteIds instanceof List<?> list) {
            List<String> values = new ArrayList<>();
            for (Object item : list) {
                values.add(String.valueOf(item));
            }
            return values;
        }
        return List.of();
    }

    public boolean isAccessToken(String token) {
        return "access".equals(parseToken(token).get("type", String.class));
    }

    public boolean isServiceTokenForInternalAudience(String token) {
        Claims claims = parseToken(token);
        if (!"service".equals(claims.get("type", String.class))) {
            return false;
        }

        Collection<String> audiences = claims.getAudience();
        return audiences != null && audiences.contains(internalAudience);
    }

    public boolean isTokenValid(String token) {
        try {
            parseToken(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
