package com.orchestrix.service;

import com.orchestrix.repository.RefreshTokenRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class RefreshTokenCleanupScheduler {

    private static final Logger logger = LoggerFactory.getLogger(RefreshTokenCleanupScheduler.class);

    private final RefreshTokenRepository refreshTokenRepository;

    public RefreshTokenCleanupScheduler(RefreshTokenRepository refreshTokenRepository) {
        this.refreshTokenRepository = refreshTokenRepository;
    }

    @Scheduled(cron = "${cleanup.cron}")
    public void cleanupExpiredTokens() {
        refreshTokenRepository.deleteExpiredOrRevoked(LocalDateTime.now());
        logger.info("Expired and revoked refresh tokens cleaned up");
    }
}
