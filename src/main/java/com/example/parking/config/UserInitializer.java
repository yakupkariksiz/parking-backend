package com.example.parking.config;

import com.example.parking.model.AppUser;
import com.example.parking.repository.AppUserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.security.SecureRandom;
import java.util.Base64;

@Configuration
public class UserInitializer {

    private static final Logger log = LoggerFactory.getLogger(UserInitializer.class);

    @Bean
    CommandLineRunner initUsers(AppUserRepository userRepository,
                                PasswordEncoder passwordEncoder,
                                @Value("${app.init.admin-username:admin}") String adminUsername,
                                @Value("${app.init.admin-password:}") String adminPassword) {
        return args -> {
            if (userRepository.count() > 0) {
                return; // users already exist, skip seeding
            }

            String password = adminPassword;
            boolean generated = false;
            if (password == null || password.isBlank()) {
                // Generate a random 20-char password and print it once
                byte[] bytes = new byte[15];
                new SecureRandom().nextBytes(bytes);
                password = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
                generated = true;
            }

            AppUser admin = new AppUser(
                    adminUsername,
                    passwordEncoder.encode(password),
                    "ROLE_ADMIN",
                    true
            );

            userRepository.save(admin);

            if (generated) {
                log.warn("=======================================================");
                log.warn("First-run: created admin user '{}'", adminUsername);
                log.warn("Generated password: {}", password);
                log.warn("Change this password immediately after first login!");
                log.warn("=======================================================");
            } else {
                log.info("Created admin user '{}' from environment configuration.", adminUsername);
            }
        };
    }
}
