package com.amalitech.smartshop.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfigurationSource;

/**
 * Central Spring Security configuration.
 * Configures JWT-based stateless authentication, CORS, CSRF,
 * OAuth2 login, and role-based access control.
 *
 * <p>Two filter chains are defined:
 * <ul>
 *   <li><b>csrfDemoFilterChain</b> (Order 1) — Handles the /api/csrf-demo/** form-based
 *       endpoints with CSRF protection ENABLED. This illustrates how CSRF tokens protect
 *       server-rendered forms from cross-site request forgery attacks.</li>
 *   <li><b>filterChain</b> (Order 2) — Main API filter chain with CSRF DISABLED. This is
 *       appropriate for stateless JWT APIs where tokens are sent via the Authorization
 *       header, not cookies, making CSRF attacks inapplicable.</li>
 * </ul>
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final CustomUserDetailsService userDetailsService;
    private final CorsConfigurationSource corsConfigurationSource;
    private final OAuth2LoginSuccessHandler oAuth2LoginSuccessHandler;
    private final CustomOAuth2UserService customOAuth2UserService;
    private final CustomAccessDeniedHandler accessDeniedHandler;
    private final CustomAuthenticationEntryPoint authenticationEntryPoint;

    /**
     * CSRF-demo filter chain: illustrates CSRF protection on a form-based endpoint.
     * CSRF is ENABLED here because the form uses session-based submission (cookies),
     * making it vulnerable to cross-site request forgery without a CSRF token.
     */
    @Bean
    @Order(1)
    public SecurityFilterChain csrfDemoFilterChain(HttpSecurity http) throws Exception {
        return http
                .securityMatcher("/api/csrf-demo/**")
                .csrf(csrf -> csrf.ignoringRequestMatchers("/api/csrf-demo/token"))
                .cors(cors -> cors.configurationSource(corsConfigurationSource))
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().permitAll()
                )
                .build();
    }

    /**
     * Main API filter chain for JWT-based stateless authentication.
     * CSRF is DISABLED because JWT tokens are transmitted in the Authorization header,
     * not in cookies, so CSRF attacks are not applicable.
     */
    @Bean
    @Order(2)
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
                // CSRF disabled for stateless JWT API — tokens are not stored in cookies,
                // so CSRF attacks are not applicable for Bearer token authentication
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource))
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .authorizeHttpRequests(auth -> auth
                        // Public auth endpoints
                        .requestMatchers("/api/auth/login", "/api/auth/register").permitAll()

                        // OAuth2 endpoints
                        .requestMatchers("/oauth2/**", "/login/oauth2/**").permitAll()

                        // Public product/category browsing (GET only)
                        .requestMatchers(HttpMethod.GET, "/api/products/**", "/api/categories/**").permitAll()

                        // Swagger/OpenAPI documentation
                        .requestMatchers(
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/v3/api-docs/**",
                                "/swagger-resources/**",
                                "/swagger-custom.css"
                        ).permitAll()

                        // GraphQL endpoint (secured by method-level annotations)
                        .requestMatchers("/graphql", "/graphiql/**").permitAll()

                        // Actuator health endpoint
                        .requestMatchers("/actuator/health").permitAll()

                        // Admin-only endpoints
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")

                        // Staff endpoints accessible by ADMIN and STAFF
                        .requestMatchers("/api/staff/**").hasAnyRole("ADMIN", "STAFF")

                        // All other requests require authentication
                        .anyRequest().authenticated()
                )
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .exceptionHandling(ex -> ex
                        .accessDeniedHandler(accessDeniedHandler)
                        .authenticationEntryPoint(authenticationEntryPoint)
                )
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .oauth2Login(oauth2 -> oauth2
                        .userInfoEndpoint(userInfo -> userInfo.userService(customOAuth2UserService))
                        .successHandler(oAuth2LoginSuccessHandler)

                )
                .build();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
