package com.example.parking.service;

import com.example.parking.dto.RegistrationRequestResponse;
import com.example.parking.model.AppUser;
import com.example.parking.model.RegistrationRequest;
import com.example.parking.model.RegistrationRequest.Status;
import com.example.parking.repository.AppUserRepository;
import com.example.parking.repository.RegistrationRequestRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class RegistrationService {

    private static final Logger log = LoggerFactory.getLogger(RegistrationService.class);
    private static final int TOKEN_EXPIRY_HOURS = 48;

    private final RegistrationRequestRepository registrationRepository;
    private final AppUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    public RegistrationService(RegistrationRequestRepository registrationRepository,
                               AppUserRepository userRepository,
                               PasswordEncoder passwordEncoder,
                               EmailService emailService) {
        this.registrationRepository = registrationRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
    }

    /**
     * Submit a new registration request (public endpoint).
     */
    @Transactional
    public void submitRequest(String email) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email is required");
        }

        email = email.trim().toLowerCase();

        // Check if email already registered as a user
        if (userRepository.findByEmail(email).isPresent()) {
            throw new IllegalArgumentException("This email is already registered");
        }

        // Check if there's already a pending/approved request for this email
        var existing = registrationRepository.findByEmail(email);
        if (existing.isPresent()) {
            Status status = existing.get().getStatus();
            if (status == Status.PENDING) {
                throw new IllegalArgumentException("A registration request for this email is already pending");
            }
            if (status == Status.APPROVED) {
                throw new IllegalArgumentException("This email has already been approved. Check your inbox for the setup link.");
            }
            // If REJECTED, allow re-submission by updating status
            RegistrationRequest req = existing.get();
            req.setStatus(Status.PENDING);
            req.setToken(null);
            req.setTokenExpiry(null);
            req.setApprovedRole(null);
            req.setCreatedAt(LocalDateTime.now());
            registrationRepository.save(req);
            log.info("Re-submitted registration request for: {}", email);
            return;
        }

        RegistrationRequest request = new RegistrationRequest(email);
        registrationRepository.save(request);
        log.info("New registration request submitted for: {}", email);
    }

    /**
     * Get all registration requests (admin endpoint).
     */
    public List<RegistrationRequestResponse> getAllRequests() {
        return registrationRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(this::toResponse)
                .toList();
    }

    /**
     * Get only pending registration requests (admin endpoint).
     */
    public List<RegistrationRequestResponse> getPendingRequests() {
        return registrationRepository.findByStatusOrderByCreatedAtDesc(Status.PENDING).stream()
                .map(this::toResponse)
                .toList();
    }

    /**
     * Approve a registration request and send setup email (admin endpoint).
     */
    @Transactional
    public void approveRequest(Long id, String role) {
        RegistrationRequest request = registrationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Registration request not found"));

        if (request.getStatus() != Status.PENDING) {
            throw new IllegalArgumentException("This request has already been " + request.getStatus().name().toLowerCase());
        }

        // Validate role
        if (role == null || (!role.equals("ROLE_ADMIN") && !role.equals("ROLE_USER"))) {
            throw new IllegalArgumentException("Role must be ROLE_ADMIN or ROLE_USER");
        }

        // Generate token
        String token = UUID.randomUUID().toString();
        request.setStatus(Status.APPROVED);
        request.setApprovedRole(role);
        request.setToken(token);
        request.setTokenExpiry(LocalDateTime.now().plusHours(TOKEN_EXPIRY_HOURS));
        registrationRepository.save(request);

        // Send email
        emailService.sendPasswordSetupEmail(request.getEmail(), token);

        log.info("Approved registration for {} with role {}", request.getEmail(), role);
    }

    /**
     * Reject a registration request (admin endpoint).
     */
    @Transactional
    public void rejectRequest(Long id) {
        RegistrationRequest request = registrationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Registration request not found"));

        if (request.getStatus() != Status.PENDING) {
            throw new IllegalArgumentException("This request has already been " + request.getStatus().name().toLowerCase());
        }

        request.setStatus(Status.REJECTED);
        registrationRepository.save(request);
        log.info("Rejected registration for: {}", request.getEmail());
    }

    /**
     * Validate a password-setup token (public endpoint).
     */
    public RegistrationRequest validateToken(String token) {
        if (token == null || token.isBlank()) {
            throw new IllegalArgumentException("Token is required");
        }

        RegistrationRequest request = registrationRepository.findByToken(token)
                .orElseThrow(() -> new IllegalArgumentException("Invalid or expired token"));

        if (request.getStatus() != Status.APPROVED) {
            throw new IllegalArgumentException("Invalid token");
        }

        if (request.getTokenExpiry() != null && request.getTokenExpiry().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("This link has expired. Please contact an administrator.");
        }

        return request;
    }

    /**
     * Complete registration: set username and password, create AppUser (public endpoint).
     */
    @Transactional
    public void completeRegistration(String token, String username, String password) {
        RegistrationRequest request = validateToken(token);

        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("Username is required");
        }
        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException("Password is required");
        }
        if (password.length() < 6) {
            throw new IllegalArgumentException("Password must be at least 6 characters");
        }

        // Check username uniqueness
        if (userRepository.findByUsername(username.trim()).isPresent()) {
            throw new IllegalArgumentException("Username already exists");
        }

        // Check email uniqueness (should be unique but double-check)
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new IllegalArgumentException("This email is already registered as a user");
        }

        // Create the user
        AppUser user = new AppUser(
                username.trim(),
                passwordEncoder.encode(password),
                request.getApprovedRole(),
                true
        );
        user.setEmail(request.getEmail());
        userRepository.save(user);

        // Remove the registration request (it's been used)
        registrationRepository.delete(request);

        log.info("Registration completed for {} (email: {}, role: {})",
                username, request.getEmail(), request.getApprovedRole());
    }

    private RegistrationRequestResponse toResponse(RegistrationRequest request) {
        return new RegistrationRequestResponse(
                request.getId(),
                request.getEmail(),
                request.getStatus().name(),
                request.getApprovedRole(),
                request.getCreatedAt()
        );
    }
}
