package com.amalitech.smartshop.config;

import com.amalitech.smartshop.entities.Session;
import com.amalitech.smartshop.entities.User;
import com.amalitech.smartshop.exceptions.UnauthorizedException;
import com.amalitech.smartshop.interfaces.SessionService;
import com.amalitech.smartshop.repositories.jpa.UserJpaRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Interceptor for authentication.
 * Validates Bearer tokens and sets authenticated user context.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AuthInterceptor implements HandlerInterceptor {

    private final SessionService sessionService;
    private final UserJpaRepository userRepository;

    @Override
    public boolean preHandle(HttpServletRequest request, @NonNull HttpServletResponse response, Object handler) throws Exception {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        String requestURI = request.getRequestURI();
        boolean isPublicEndpoint = requestURI.contains("/public/");

        String authHeader = request.getHeader("Authorization");
        log.info("Auth Header: {}", authHeader);

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            if (isPublicEndpoint) {
                return true;
            }
            throw new UnauthorizedException("Missing or invalid Authorization header");
        }

        String token = authHeader.substring(7);

        try {
            Session session = sessionService.validateSession(token)
                    .orElseThrow(() -> new UnauthorizedException("Invalid or expired session"));
            
            User user = userRepository.findById(session.getUserId())
                    .orElseThrow(() -> new UnauthorizedException("User not found"));
            
            request.setAttribute("authUserId", session.getUserId());
            request.setAttribute("authenticatedUserRole", user.getRole().name());
            request.setAttribute("sessionToken", token);
            
            return true;
        } catch (UnauthorizedException e) {
            if (isPublicEndpoint) {
                return true;
            }
            throw e;
        }
    }
}
