package com.amalitech.smartshop.config;

import com.amalitech.smartshop.exceptions.UnauthorizedException;
import com.amalitech.smartshop.security.JwtService;
import com.amalitech.smartshop.security.TokenBlacklistService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.graphql.server.WebGraphQlInterceptor;
import org.springframework.graphql.server.WebGraphQlRequest;
import org.springframework.graphql.server.WebGraphQlResponse;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * GraphQL interceptor for JWT-based authentication.
 * Validates Bearer JWT tokens and sets authenticated user context for GraphQL operations.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class GraphQLAuthInterceptor implements WebGraphQlInterceptor {

    private final JwtService jwtService;
    private final TokenBlacklistService tokenBlacklistService;
    private static final List<String> PUBLIC_QUERIES = List.of("allProducts", "productById", "allCategories", "categoryById");
    private static final List<String> PUBLIC_MUTATIONS = List.of("login", "register");

    @Override
    public Mono<WebGraphQlResponse> intercept(WebGraphQlRequest request, Chain chain) {
        String authHeader = request.getHeaders().getFirst("Authorization");
        String document = request.getDocument();

        if (document != null && (document.contains("__schema") || document.contains("IntrospectionQuery"))) {
            return chain.next(request);
        }

        boolean isPublic = PUBLIC_QUERIES.stream().anyMatch(op -> document != null && document.contains(op)) ||
                PUBLIC_MUTATIONS.stream().anyMatch(op -> document != null && document.contains(op));

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            if (isPublic) {
                return chain.next(request);
            }
            return Mono.error(new UnauthorizedException("Missing or invalid Authorization header"));
        }

        String token = authHeader.substring(7);

        try {
            if (tokenBlacklistService.isRevoked(token)) {
                return Mono.error(new UnauthorizedException("Token has been revoked"));
            }

            String username = jwtService.extractUsername(token);
            String role = jwtService.extractRole(token);

            if (username == null || jwtService.isTokenExpired(token)) {
                return Mono.error(new UnauthorizedException("Invalid or expired token"));
            }

            request.configureExecutionInput((executionInput, builder) ->
                    builder.graphQLContext(context -> {
                        context.put("username", username);
                        context.put("userRole", role);
                    }).build()
            );

            return chain.next(request);
        } catch (UnauthorizedException e) {
            if (isPublic) {
                return chain.next(request);
            }
            return Mono.error(e);
        } catch (Exception e) {
            log.warn("JWT validation failed for GraphQL request: {}", e.getMessage());
            if (isPublic) {
                return chain.next(request);
            }
            return Mono.error(new UnauthorizedException("Invalid token"));
        }
    }
}
