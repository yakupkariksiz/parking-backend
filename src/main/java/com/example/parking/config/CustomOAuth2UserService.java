package com.example.parking.config;

import com.example.parking.model.AppUser;
import com.example.parking.repository.AppUserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private static final Logger log = LoggerFactory.getLogger(CustomOAuth2UserService.class);

    private final AppUserRepository appUserRepository;

    // List of allowed email addresses for ADMIN role
    private static final List<String> ALLOWED_ADMIN_EMAILS = List.of(
            "yakupkariksiz@gmail.com"  // Add your Google email here
    );

    public CustomOAuth2UserService(AppUserRepository appUserRepository) {
        this.appUserRepository = appUserRepository;
    }

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        log.info("OAuth2 login attempt...");

        OAuth2User oauth2User = super.loadUser(userRequest);

        String provider = userRequest.getClientRegistration().getRegistrationId(); // "google"
        String providerId = oauth2User.getAttribute("sub"); // Google's user ID
        String email = oauth2User.getAttribute("email");
        String name = oauth2User.getAttribute("name");

        log.info("OAuth2 user info - provider: {}, email: {}, name: {}", provider, email, name);

        if (email == null) {
            log.error("Email not found from OAuth2 provider");
            throw new OAuth2AuthenticationException("Email not found from OAuth2 provider");
        }

        AppUser appUser;

        try {
            // First, try to find by email (most reliable)
            Optional<AppUser> existingByEmail = appUserRepository.findByEmail(email);

            if (existingByEmail.isPresent()) {
                appUser = existingByEmail.get();
                log.info("Found existing user by email: {}", email);

                // Update provider info if not set
                if (appUser.getProvider() == null || !appUser.getProvider().equals(provider)) {
                    appUser.setProvider(provider);
                    appUser.setProviderId(providerId);
                    appUserRepository.save(appUser);
                }
            } else {
                // Check by provider and providerId
                Optional<AppUser> existingByProvider = appUserRepository.findByProviderAndProviderId(provider, providerId);

                if (existingByProvider.isPresent()) {
                    appUser = existingByProvider.get();
                    log.info("Found existing user by provider: {}", providerId);
                    appUser.setEmail(email);
                    appUserRepository.save(appUser);
                } else {
                    // Create new user
                    log.info("Creating new OAuth2 user for email: {}", email);

                    String role = ALLOWED_ADMIN_EMAILS.stream()
                            .anyMatch(adminEmail -> adminEmail.equalsIgnoreCase(email))
                            ? "ROLE_ADMIN"
                            : "ROLE_USER";

                    // Use email as username to avoid conflicts (emails are unique)
                    String username = email;

                    appUser = new AppUser(
                            username,
                            email,
                            role,
                            provider,
                            providerId
                    );
                    appUserRepository.save(appUser);
                    log.info("Created new user with role: {}", role);
                }
            }
        } catch (Exception e) {
            log.error("Error processing OAuth2 user", e);
            throw new OAuth2AuthenticationException("Error processing OAuth2 user: " + e.getMessage());
        }

        // Return OAuth2User with proper authorities
        return new DefaultOAuth2User(
                Collections.singleton(new SimpleGrantedAuthority(appUser.getRole())),
                oauth2User.getAttributes(),
                "email" // use email as the principal name attribute
        );
    }
}

