package com.orchestrix.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "refresh_tokens")
public class RefreshToken {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "token", unique = true)
    private String token;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    @Column(name = "revoked_at")
    private LocalDateTime revokedAt;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    protected RefreshToken() {
    }

    public RefreshToken(User user, String token, int expirationWeeks) {
        this.user = user;
        this.token = token;
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.expiresAt = now.plusWeeks(expirationWeeks);
    }

    public User getUser() {
        return user;
    }

    public Long getId() {
        return id;
    }

    public String getToken() {
        return token;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public LocalDateTime getRevokedAt() {
        return revokedAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void revokeToken() {
        this.revokedAt = LocalDateTime.now();
    }
}
