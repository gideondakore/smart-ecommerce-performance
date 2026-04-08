package com.amalitech.smartshop.security;

import com.amalitech.smartshop.dtos.responses.ApiResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Custom authentication entry point that returns a JSON 401 response
 * instead of redirecting to a login page.
 * This is required because the OAuth2 login configuration registers
 * a default entry point that redirects to an HTML login page,
 * which is not appropriate for stateless JWT API clients.
 */
@Component
@Slf4j
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void commence(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull AuthenticationException authException) throws IOException {

        log.warn("Unauthorized access attempt to {} from IP: {}",
                request.getRequestURI(), request.getRemoteAddr());

        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        ApiResponse<Void> apiResponse = new ApiResponse<>(
                HttpStatus.UNAUTHORIZED.value(),
                "Authentication required. Please provide a valid JWT token.",
                null
        );

        objectMapper.writeValue(response.getOutputStream(), apiResponse);
    }
}
