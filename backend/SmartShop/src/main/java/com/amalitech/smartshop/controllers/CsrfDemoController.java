package com.amalitech.smartshop.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Demonstrates CSRF protection on a form-based endpoint.
 *
 * <p><b>Why CSRF is enabled here:</b> This endpoint simulates a traditional server-rendered
 * HTML form submission. Unlike stateless JWT APIs (where tokens are sent in the Authorization
 * header), form submissions use cookies to identify sessions. Without a CSRF token, an attacker
 * could craft a malicious page that submits a form to this endpoint using the victim's cookies,
 * performing an action without the user's consent.</p>
 *
 * <p><b>How to test:</b>
 * <ol>
 *   <li>GET /api/csrf-demo/token — retrieves the CSRF token</li>
 *   <li>POST /api/csrf-demo/feedback — submit with the token in the X-CSRF-TOKEN header
 *       or _csrf parameter. Without the token, the request is rejected with 403 Forbidden.</li>
 * </ol>
 *
 * @see com.amalitech.smartshop.security.SecurityConfig#csrfDemoFilterChain(org.springframework.security.config.annotation.web.builders.HttpSecurity)
 */
@Tag(name = "CSRF Demo", description = "Demonstrates CSRF protection on form-based endpoints")
@RestController
@RequestMapping("/api/csrf-demo")
@Slf4j
public class CsrfDemoController {

    @Operation(summary = "Get CSRF token for form submission")
    @GetMapping("/token")
    public ResponseEntity<Map<String, String>> getCsrfToken(HttpServletRequest request) {
        CsrfToken csrfToken = (CsrfToken) request.getAttribute(CsrfToken.class.getName());
        if (csrfToken == null) {
            return ResponseEntity.ok(Map.of("message", "CSRF is not active for this request"));
        }
        log.info("CSRF token issued for session from IP: {}", request.getRemoteAddr());
        return ResponseEntity.ok(Map.of(
                "token", csrfToken.getToken(),
                "headerName", csrfToken.getHeaderName(),
                "parameterName", csrfToken.getParameterName()
        ));
    }

    @Operation(summary = "Submit feedback (CSRF-protected form endpoint)")
    @PostMapping(value = "/feedback", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public ResponseEntity<Map<String, String>> submitFeedback(
            @RequestParam String message,
            HttpServletRequest request) {
        log.info("CSRF-protected feedback received from IP: {} — message length: {}",
                request.getRemoteAddr(), message.length());
        return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "Feedback submitted successfully (CSRF token was valid)"
        ));
    }
}
