package com.amalitech.smartshop.config;

import com.amalitech.smartshop.enums.UserRole;
import com.amalitech.smartshop.exceptions.UnauthorizedException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import lombok.extern.slf4j.Slf4j;

/**
 * Interceptor for role-based authorization.
 * Validates that the authenticated user has the required role.
 */
@Slf4j
@Component
public class RoleInterceptor implements HandlerInterceptor {
    
    @Override
    public boolean preHandle(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, Object handler) throws Exception {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        if (!(handler instanceof HandlerMethod handlerMethod)) return true;
        RequiresRole annotation = handlerMethod.getMethodAnnotation(RequiresRole.class);

        if (annotation == null) return true;

        String userRole = (String) request.getAttribute("authenticatedUserRole");
        log.info("Role check - Required: {}, User has: {}", annotation.value(), userRole);

        for (UserRole requiredRole : annotation.value()) {
            if (requiredRole.name().equals(userRole)) {
                return true;
            }
        }
        throw new UnauthorizedException("User does not have required role to access this resource");
    }
}
