package com.orchestrix.security;

import io.jsonwebtoken.Jwts;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;

@Service
public class JwtService {
    private static final int MILLIS_IN_SECS = 1000;
    private static final int SECS_IN_MIN = 60;
    private static final int MINS_IN_HOUR = 60;

    private final SecretKey key = Jwts.SIG.HS256.key().build();

    public String generateToken(String email) {
        return Jwts.builder()
                .subject(email)
                .issuedAt(new Date())
                .expiration(new Date(
                        System.currentTimeMillis() + MILLIS_IN_SECS * SECS_IN_MIN * MINS_IN_HOUR))
                .signWith(key)
                .compact();
    }

    public String extractEmail(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }
}
