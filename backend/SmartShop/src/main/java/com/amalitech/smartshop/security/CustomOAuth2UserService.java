package com.amalitech.smartshop.security;

import com.amalitech.smartshop.entities.User;
import com.amalitech.smartshop.enums.UserRole;
import com.amalitech.smartshop.repositories.jpa.UserJpaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.UUID;

/**
 * Fetches user details from Google OAuth2 and persists them.
 * Assigns default CUSTOMER role on first OAuth2 login.
 * Maps Google email as the unique identifier for user lookup/creation.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserJpaRepository userRepository;

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);
        Map<String, Object> attributes = oAuth2User.getAttributes();

        String email = (String) attributes.get("email");
        String firstName = (String) attributes.get("given_name");
        String lastName = (String) attributes.get("family_name");
        String googleId = (String) attributes.get("sub");
        String provider = userRequest.getClientRegistration().getRegistrationId();

        userRepository.findByEmail(email).orElseGet(() -> {
            log.info("Creating new OAuth2 user for email: {}", email);
            User newUser = User.builder()
                    .email(email)
                    .firstName(firstName != null ? firstName : "OAuth2")
                    .lastName(lastName != null ? lastName : "User")
                    .password(UUID.randomUUID().toString())
                    .role(UserRole.CUSTOMER)
                    .oauth2Provider(provider)
                    .oauth2Id(googleId)
                    .build();
            return userRepository.save(newUser);
        });

        log.info("OAuth2 login success for user: {}", email);
        return oAuth2User;
    }
}
