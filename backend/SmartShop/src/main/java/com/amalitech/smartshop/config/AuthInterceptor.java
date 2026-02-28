package com.amalitech.smartshop.config;

import com.amalitech.smartshop.entities.Session;
import com.amalitech.smartshop.entities.User;
import com.amalitech.smartshop.exceptions.UnauthorizedException;
import com.amalitech.smartshop.interfaces.SessionService;
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

    @Override
    public boolean preHandle(HttpServletRequest request, @NonNull HttpServletResponse response, Object handler) throws Exception {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        // Allow public GET requests for products and categories
        String path = request.getRequestURI();
        String method = request.getMethod();
        if ("GET".equalsIgnoreCase(method) && 
            (path.startsWith("/api/products") || path.startsWith("/api/categories"))) {
            return true;
        }

        String authHeader = request.getHeader("Authorization");
        log.info("Auth Header: {}", authHeader);

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new UnauthorizedException("Missing or invalid Authorization header");
        }

        String token = authHeader.substring(7);

        Session session = sessionService.validateSession(token)
                .orElseThrow(() -> new UnauthorizedException("Invalid or expired session"));
        
        User user = session.getUser();
        
        log.info("Authenticated user: id={}, role={}", user.getId(), user.getRole().name());
        
        request.setAttribute("authUserId", user.getId());
        request.setAttribute("authenticatedUserRole", user.getRole().name());
        request.setAttribute("sessionToken", token);
        
        return true;
    }
}
