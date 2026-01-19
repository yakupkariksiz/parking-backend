package com.example.parking.config;

import com.example.parking.model.AppUser;
import com.example.parking.repository.AppUserRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final AppUserRepository appUserRepository;

    // List of allowed email addresses (you can move this to application.properties or database)
    private static final List<String> ALLOWED_ADMIN_EMAILS = List.of(
            "yakupkariksiz@gmail.com"  // Add your Google email here
    );

    public CustomOAuth2UserService(AppUserRepository appUserRepository) {
        this.appUserRepository = appUserRepository;
    }

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oauth2User = super.loadUser(userRequest);

        String provider = userRequest.getClientRegistration().getRegistrationId(); // "google"
        String providerId = oauth2User.getAttribute("sub"); // Google's user ID
        String email = oauth2User.getAttribute("email");
        String name = oauth2User.getAttribute("name");

        if (email == null) {
            throw new OAuth2AuthenticationException("Email not found from OAuth2 provider");
        }

        // Check if user exists by provider and providerId
        Optional<AppUser> existingUser = appUserRepository.findByProviderAndProviderId(provider, providerId);

        AppUser appUser;
        if (existingUser.isPresent()) {
            appUser = existingUser.get();
            // Update email/name if changed
            appUser.setEmail(email);
            appUser.setUsername(name != null ? name : email);
            appUserRepository.save(appUser);
        } else {
            // Check if this email is allowed
            String role = ALLOWED_ADMIN_EMAILS.contains(email.toLowerCase())
                    ? "ROLE_ADMIN"
                    : "ROLE_USER";

            // Create new user
            appUser = new AppUser(
                    name != null ? name : email,
                    email,
                    role,
                    provider,
                    providerId
            );
            appUserRepository.save(appUser);
        }

        // Return OAuth2User with proper authorities
        return new DefaultOAuth2User(
                Collections.singleton(new SimpleGrantedAuthority(appUser.getRole())),
                oauth2User.getAttributes(),
                "email" // use email as the principal name attribute
        );
    }
}

