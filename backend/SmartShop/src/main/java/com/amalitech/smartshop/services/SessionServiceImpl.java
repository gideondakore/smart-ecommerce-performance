package com.amalitech.smartshop.services;

import com.amalitech.smartshop.entities.Session;
import com.amalitech.smartshop.entities.User;
import com.amalitech.smartshop.exceptions.ResourceNotFoundException;
import com.amalitech.smartshop.interfaces.SessionService;
import com.amalitech.smartshop.repositories.jpa.SessionJpaRepository;
import com.amalitech.smartshop.repositories.jpa.UserJpaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class SessionServiceImpl implements SessionService {

    private final SessionJpaRepository sessionRepository;
    private final UserJpaRepository userRepository;
    private static final int SESSION_DURATION_HOURS = 24;

    @Override
    @Transactional
    public String createSession(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));

        String token = UUID.randomUUID().toString();
        LocalDateTime expiresAt = LocalDateTime.now().plusHours(SESSION_DURATION_HOURS);

        Session session = Session.builder()
                .token(token)
                .user(user)
                .expiresAt(expiresAt)
                .build();

        sessionRepository.save(session);
        log.info("Created session for user: {}, expires at: {}", userId, expiresAt);
        return token;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Session> validateSession(String token) {
        return sessionRepository.findByToken(token);
    }

    @Override
    @Transactional
    public void deleteSession(String token) {
        sessionRepository.deleteByToken(token);
        log.info("Deleted session: {}", token);
    }

    @Override
    @Transactional
    public void deleteAllUserSessions(Long userId) {
        sessionRepository.deleteByUser_Id(userId);
        log.info("Deleted all sessions for user: {}", userId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isSessionValid(String token) {
        return sessionRepository.existsValidSession(token, LocalDateTime.now());
    }

    @Override
    @Transactional
    public int cleanExpiredSessions() {
        int deleted = sessionRepository.deleteExpiredSessions(LocalDateTime.now());
        log.info("Cleaned up {} expired sessions", deleted);
        return deleted;
    }

    @Override
    @Transactional(readOnly = true)
    public Long countActiveSessions() {
        return sessionRepository.countActiveSessions();
    }
}
