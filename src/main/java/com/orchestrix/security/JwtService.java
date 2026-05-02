package com.orchestrix.security;

import com.orchestrix.entity.Role;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;

@Service
public class JwtService {
    private static final int MILLIS_IN_SECS = 1000;
    private static final int SECS_IN_MIN = 60;
    private static final int MINS_IN_HOUR = 60;

    @Value("${jwt.secret}")
    private String secret;

    private SecretKey key;

    @PostConstruct
    private void init() {
        this.key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
    }

    public String generateToken(String email, Role role) {
        return Jwts.builder()
                .subject(email)
                .claim("role", role.name())
                .issuedAt(new Date())
                .expiration(new Date(
                        System.currentTimeMillis() + MILLIS_IN_SECS * SECS_IN_MIN * MINS_IN_HOUR))
                .signWith(key)
                .compact();
    }

    public String extractEmail(String token) {
        return extractClaims(token).getPayload().getSubject();
    }

    public String extractRole(String token) {
        return extractClaims(token).getPayload().get("role", String.class);
    }

    private Jws<Claims> extractClaims(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token);
    }
}
