package com.silemore.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JwtUtil {
    private final SecretKey secretKey;
    private final long expirationSeconds;
    private final long rememberMeSeconds;

    public JwtUtil(@Value("${jwt.secret}") String secret,
                   @Value("${jwt.expiration-seconds}") long expirationSeconds,
                   @Value("${jwt.remember-me-seconds}") long rememberMeSeconds) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expirationSeconds = expirationSeconds;
        this.rememberMeSeconds = rememberMeSeconds;
    }

    public String generateToken(Long userId, String email, boolean rememberMe) {
        long nowMillis = System.currentTimeMillis();
        long ttlSeconds = getExpirationSeconds(rememberMe);
        Date expiry = new Date(nowMillis + ttlSeconds * 1000);
        return Jwts.builder()
                .setSubject(String.valueOf(userId))
                .claim("email", email)
                .setIssuedAt(new Date(nowMillis))
                .setExpiration(expiry)
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();
    }

    public Claims parseToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public long getExpirationSeconds(boolean rememberMe) {
        return rememberMe ? rememberMeSeconds : expirationSeconds;
    }
}
