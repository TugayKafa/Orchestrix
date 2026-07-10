package com.orchestrix.security;

import com.orchestrix.entity.auth.Role;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.crypto.SecretKey;
import java.lang.reflect.Field;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class JwtServiceTest {

    private JwtService jwtService;

    @BeforeEach
    void setUp() throws Exception {
        jwtService = new JwtService();
        Field secretField = JwtService.class.getDeclaredField("secret");
        secretField.setAccessible(true);
        secretField.set(jwtService, "dGVzdHNlY3JldGtleXRoYXRpc2xvbmdlbm91Z2hmb3JocyEhIQ==");

        Field expirationField = JwtService.class.getDeclaredField("expirationMs");
        expirationField.setAccessible(true);
        expirationField.set(jwtService, 3600000L);

        java.lang.reflect.Method initMethod = JwtService.class.getDeclaredMethod("init");
        initMethod.setAccessible(true);
        initMethod.invoke(jwtService);
    }

    @Test
    void testGenerateTokenReturnsValidToken() {
        String token = jwtService.generateToken("ivan@gmail.com", Role.USER);
        assertNotNull(
                token,
                "Expected a valid (non-null) token to be generated."
        );
        assertFalse(
                token.isBlank(),
                "Expected a valid (non-blank) token to be generated."
        );
    }

    @Test
    void testExtractEmailReturnsValidEmail() {
        String token = jwtService.generateToken("ivan@gmail.com", Role.USER);

        assertEquals(
                "ivan@gmail.com",
                jwtService.extractEmail(token),
                "Expected extracted email to match 'ivan@gmail.com'"
        );
    }

    @Test
    void testExtractRoleReturnsValidRole() {
        String token = jwtService.generateToken("ivan@gmail.com", Role.USER);

        assertEquals(
                Role.USER,
                jwtService.extractRole(token),
                "Expected extracted role to match Role.USER"
        );
    }

    @Test
    void testExpiredTokenThrowsException() {
        SecretKey key = Keys.hmacShaKeyFor(
                Decoders.BASE64.decode("dGVzdHNlY3JldGtleXRoYXRpc2xvbmdlbm91Z2hmb3JocyEhIQ==")
        );

        String expiredToken = Jwts.builder()
                .subject("ivan@gmail.com")
                .claim("role", "USER")
                .issuedAt(new Date(System.currentTimeMillis() - 7200000))
                .expiration(new Date(System.currentTimeMillis() - 3600000))
                .signWith(key)
                .compact();

        assertThrows(
                ExpiredJwtException.class,
                () -> jwtService.extractEmail(expiredToken),
                "Expected ExpiredJwtException to be thrown."
        );
    }
}
