package com.amalitech.smartshop.config;

import com.amalitech.smartshop.entities.Session;
import com.amalitech.smartshop.entities.User;
import com.amalitech.smartshop.exceptions.UnauthorizedException;
import com.amalitech.smartshop.interfaces.SessionService;
import com.amalitech.smartshop.repositories.jpa.UserJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.graphql.server.WebGraphQlInterceptor;
import org.springframework.graphql.server.WebGraphQlRequest;
import org.springframework.graphql.server.WebGraphQlResponse;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * GraphQL interceptor for authentication.
 * Validates Bearer tokens and sets authenticated user context for GraphQL operations.
 */
@Component
@RequiredArgsConstructor
public class GraphQLAuthInterceptor implements WebGraphQlInterceptor {

    private final SessionService sessionService;
    private final UserJpaRepository userRepository;
    private static final List<String> PUBLIC_QUERIES = List.of("allProducts", "productById", "allCategories", "categoryById");
    private static final List<String> PUBLIC_MUTATIONS = List.of("login", "register");

    @Override
    public Mono<WebGraphQlResponse> intercept(WebGraphQlRequest request, Chain chain) {
        String authHeader = request.getHeaders().getFirst("Authorization");
        String document = request.getDocument();

        // Allow introspection queries (used by GraphQL clients like GraphiQL, Apollo, etc.)
        if (document != null && (document.contains("__schema") || document.contains("IntrospectionQuery"))) {
            return chain.next(request);
        }

        // Check if it's a public query or mutation
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
            // Validate session using SessionService (same as REST API)
            Session session = sessionService.validateSession(token)
                    .orElseThrow(() -> new UnauthorizedException("Invalid or expired session"));

            // Get user from session
            User user = userRepository.findById(session.getUserId())
                    .orElseThrow(() -> new UnauthorizedException("User not found"));

            // Set user context for GraphQL resolvers
            request.configureExecutionInput((executionInput, builder) ->
                    builder.graphQLContext(context -> {
                        context.put("userId", user.getId());
                        context.put("userRole", user.getRole().name());
                    }).build()
            );

            return chain.next(request);
        } catch (UnauthorizedException e) {
            if (isPublic) {
                return chain.next(request);
            }
            return Mono.error(e);
        }
    }
}
