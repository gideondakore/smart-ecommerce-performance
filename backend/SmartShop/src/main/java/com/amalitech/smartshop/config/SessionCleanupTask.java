package com.amalitech.smartshop.config;

import com.amalitech.smartshop.repositories.jpa.SessionJpaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Component
@Slf4j
@RequiredArgsConstructor
public class SessionCleanupTask {

    private final SessionJpaRepository sessionRepository;

    @Transactional
    @Scheduled(cron = "0 0 * * * *") // Every hour
    public void cleanupExpiredSessions() {
        log.info("Running session cleanup task");
        int deletedCount = sessionRepository.deleteExpiredSessions(LocalDateTime.now());
        log.info("Deleted {} expired sessions", deletedCount);
    }
}
