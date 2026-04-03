package com.example.parking.service;

import com.example.parking.dto.CreateUserRequest;
import com.example.parking.dto.UpdateUserRequest;
import com.example.parking.dto.UserResponse;
import com.example.parking.model.AppUser;
import com.example.parking.repository.AppUserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserManagementService {

    private final AppUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserManagementService(AppUserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    public UserResponse createUser(CreateUserRequest request) {
        if (request.username() == null || request.username().isBlank()) {
            throw new IllegalArgumentException("Username is required");
        }
        if (request.password() == null || request.password().isBlank()) {
            throw new IllegalArgumentException("Password is required");
        }

        if (userRepository.findByUsername(request.username()).isPresent()) {
            throw new IllegalArgumentException("Username already exists: " + request.username());
        }

        if (request.email() != null && !request.email().isBlank()) {
            if (userRepository.findByEmail(request.email()).isPresent()) {
                throw new IllegalArgumentException("Email already exists: " + request.email());
            }
        }

        String role = request.role() != null ? request.role() : "ROLE_USER";
        if (!role.equals("ROLE_ADMIN") && !role.equals("ROLE_USER")) {
            throw new IllegalArgumentException("Role must be ROLE_ADMIN or ROLE_USER");
        }

        AppUser user = new AppUser(
                request.username(),
                passwordEncoder.encode(request.password()),
                role,
                true
        );
        if (request.email() != null && !request.email().isBlank()) {
            user.setEmail(request.email());
        }

        userRepository.save(user);
        return toResponse(user);
    }

    public UserResponse updateUser(Long id, UpdateUserRequest request, String currentUsername) {
        AppUser user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + id));

        if (request.role() != null) {
            if (!request.role().equals("ROLE_ADMIN") && !request.role().equals("ROLE_USER")) {
                throw new IllegalArgumentException("Role must be ROLE_ADMIN or ROLE_USER");
            }
            // Prevent admin from removing their own admin role
            if (user.getUsername().equals(currentUsername) && !request.role().equals("ROLE_ADMIN")) {
                throw new IllegalArgumentException("Cannot remove your own admin role");
            }
            user.setRole(request.role());
        }

        if (request.email() != null) {
            if (!request.email().isBlank()) {
                var existing = userRepository.findByEmail(request.email());
                if (existing.isPresent() && !existing.get().getId().equals(id)) {
                    throw new IllegalArgumentException("Email already exists: " + request.email());
                }
                user.setEmail(request.email());
            } else {
                user.setEmail(null);
            }
        }

        if (request.password() != null && !request.password().isBlank()) {
            user.setPassword(passwordEncoder.encode(request.password()));
        }

        if (request.enabled() != null) {
            // Prevent admin from disabling themselves
            if (user.getUsername().equals(currentUsername) && !request.enabled()) {
                throw new IllegalArgumentException("Cannot disable your own account");
            }
            user.setEnabled(request.enabled());
        }

        userRepository.save(user);
        return toResponse(user);
    }

    public void deleteUser(Long id, String currentUsername) {
        AppUser user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + id));

        if (user.getUsername().equals(currentUsername)) {
            throw new IllegalArgumentException("Cannot delete your own account");
        }

        userRepository.deleteById(id);
    }

    private UserResponse toResponse(AppUser user) {
        return new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getRole(),
                user.isEnabled()
        );
    }
}
