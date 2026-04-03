package com.example.parking.config;

import com.example.parking.model.AppUser;
import com.example.parking.repository.AppUserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class UserInitializer {

    @Bean
    CommandLineRunner initUsers(AppUserRepository userRepository,
                                PasswordEncoder passwordEncoder) {
        return args -> {
            if (userRepository.count() > 0) {
                return; // users already exist, skip seeding
            }

            AppUser admin = new AppUser(
                    "admin",
                    passwordEncoder.encode("admin123"), // CHANGE PASSWORD
                    "ROLE_ADMIN",
                    true
            );

            AppUser yakup = new AppUser(
                    "yakup",
                    passwordEncoder.encode("karixiz123"), // CHANGE PASSWORD
                    "ROLE_ADMIN",
                    true
            );

            AppUser user = new AppUser(
                    "scanner",
                    passwordEncoder.encode("scanner123"), // CHANGE PASSWORD
                    "ROLE_USER",
                    true
            );

            userRepository.save(admin);
            userRepository.save(yakup);
            userRepository.save(user);
        };
    }
}
