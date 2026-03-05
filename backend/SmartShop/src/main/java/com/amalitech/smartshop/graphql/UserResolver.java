package com.amalitech.smartshop.graphql;

import com.amalitech.smartshop.config.GraphQLRequiresRole;
import com.amalitech.smartshop.dtos.requests.AuthLoginRequest;
import com.amalitech.smartshop.dtos.requests.AuthRegisterRequest;
import com.amalitech.smartshop.dtos.responses.AuthResponse;
import com.amalitech.smartshop.dtos.responses.UserSummaryDTO;
import com.amalitech.smartshop.enums.UserRole;
import com.amalitech.smartshop.interfaces.UserService;
import com.amalitech.smartshop.security.AuthService;
import graphql.schema.DataFetchingEnvironment;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.List;

/**
 * GraphQL resolver for user-related queries and mutations.
 */
@Controller
@RequiredArgsConstructor
public class UserResolver {

    private final UserService userService;
    private final AuthService authService;

    @QueryMapping
    @GraphQLRequiresRole(UserRole.ADMIN)
    public List<UserSummaryDTO> getAllUsers(DataFetchingEnvironment env) {
        return userService.getAllUsers(Pageable.unpaged()).getContent();
    }

    @MutationMapping
    public AuthResponse login(@Argument LoginInput input) {
        AuthLoginRequest request = AuthLoginRequest.builder()
                .email(input.email())
                .password(input.password())
                .build();
        return authService.login(request, "graphql");
    }

    @MutationMapping
    public AuthResponse register(@Argument RegisterInput input) {
        AuthRegisterRequest request = AuthRegisterRequest.builder()
                .firstName(input.firstName())
                .lastName(input.lastName())
                .email(input.email())
                .password(input.password())
                .role(input.role())
                .build();
        return authService.register(request);
    }

    public record LoginInput(String email, String password) {}

    public record RegisterInput(String firstName, String lastName, String email, String password, UserRole role) {}
}
