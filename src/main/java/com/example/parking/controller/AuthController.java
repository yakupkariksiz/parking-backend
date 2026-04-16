package com.example.parking.controller;

import com.example.parking.config.JwtUtil;
import com.example.parking.event.AuditEventPublisher;
import com.example.parking.event.EventType;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final AuditEventPublisher auditEventPublisher;

    public AuthController(AuthenticationManager authenticationManager,
                          JwtUtil jwtUtil,
                          AuditEventPublisher auditEventPublisher) {
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.auditEventPublisher = auditEventPublisher;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request, HttpServletRequest httpRequest) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.username(), request.password())
            );

            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            String token = jwtUtil.generateToken(userDetails);

            String role = userDetails.getAuthorities().stream()
                    .findFirst()
                    .map(a -> a.getAuthority())
                    .orElse("ROLE_USER");

            auditEventPublisher.publish(EventType.LOGIN_SUCCESS,
                    "User '" + userDetails.getUsername() + "' logged in",
                    userDetails.getUsername(), httpRequest);

            return ResponseEntity.ok(Map.of(
                    "token", token,
                    "username", userDetails.getUsername(),
                    "role", role
            ));
        } catch (DisabledException e) {
            auditEventPublisher.publish(EventType.LOGIN_FAILURE,
                    "Login failed for disabled account '" + request.username() + "'",
                    request.username(), httpRequest);
            return ResponseEntity.status(403).body(Map.of("error", "Account is disabled"));
        } catch (BadCredentialsException e) {
            auditEventPublisher.publish(EventType.LOGIN_FAILURE,
                    "Failed login attempt for '" + request.username() + "' — bad credentials",
                    request.username(), httpRequest);
            return ResponseEntity.status(401).body(Map.of("error", "Invalid username or password"));
        }
    }

    public record LoginRequest(String username, String password) {}
}
