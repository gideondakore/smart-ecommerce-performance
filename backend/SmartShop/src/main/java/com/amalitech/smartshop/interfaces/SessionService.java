package com.amalitech.smartshop.interfaces;

import com.amalitech.smartshop.entities.Session;

import java.util.Optional;

public interface SessionService {
    String createSession(Long userId);
    Optional<Session> validateSession(String token);
    void deleteSession(String token);
    void deleteAllUserSessions(Long userId);
    boolean isSessionValid(String token);
    Long countActiveSessions();
    int cleanExpiredSessions();
}
