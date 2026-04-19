package com.psygst.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT provider — post UUID migration.
 * Claims idAuth, idProfesional, idSistema, idRol are now String (UUID or numeric string).
 * Existing tokens with Integer claims will fail validation and force re-login (expected behavior).
 */
@Component
@Slf4j
public class JwtProvider {

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expiration-ms}")
    private long jwtExpirationMs;

    private SecretKey getSigningKey() {
        byte[] keyBytes = jwtSecret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateToken(String idAuth, String idProfesional, String idSistema, String idRol,
            String username) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("idAuth",        idAuth);
        claims.put("idProfesional", idProfesional);
        claims.put("idSistema",     idSistema);
        claims.put("idRol",         idRol);

        return Jwts.builder()
                .subject(username)
                .claims(claims)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + jwtExpirationMs))
                .signWith(getSigningKey())
                .compact();
    }

    public Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public boolean validateToken(String token) {
        try {
            extractAllClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            log.warn("JWT validation failed: {}", e.getMessage());
            return false;
        }
    }

    public String extractUsername(String token) {
        return extractAllClaims(token).getSubject();
    }

    public String extractIdSistema(String token) {
        return extractAllClaims(token).get("idSistema", String.class);
    }

    public String extractIdProfesional(String token) {
        return extractAllClaims(token).get("idProfesional", String.class);
    }

    public String extractIdRol(String token) {
        return extractAllClaims(token).get("idRol", String.class);
    }

    public String extractIdAuth(String token) {
        return extractAllClaims(token).get("idAuth", String.class);
    }
}
