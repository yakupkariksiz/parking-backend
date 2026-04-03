package com.example.parking.controller;

import com.example.parking.dto.RegistrationRequestResponse;
import com.example.parking.service.RegistrationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
public class RegistrationController {

    private final RegistrationService registrationService;

    public RegistrationController(RegistrationService registrationService) {
        this.registrationService = registrationService;
    }

    // --- Public endpoints ---

    @PostMapping("/api/auth/register")
    public ResponseEntity<?> register(@RequestBody Map<String, String> body) {
        try {
            registrationService.submitRequest(body.get("email"));
            return ResponseEntity.ok(Map.of("message",
                    "Registration request submitted. An administrator will review your request."));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/api/auth/validate-token")
    public ResponseEntity<?> validateToken(@RequestParam String token) {
        try {
            var request = registrationService.validateToken(token);
            return ResponseEntity.ok(Map.of(
                    "valid", true,
                    "email", request.getEmail()
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "valid", false,
                    "error", e.getMessage()
            ));
        }
    }

    @PostMapping("/api/auth/set-password")
    public ResponseEntity<?> setPassword(@RequestBody Map<String, String> body) {
        try {
            registrationService.completeRegistration(
                    body.get("token"),
                    body.get("username"),
                    body.get("password")
            );
            return ResponseEntity.ok(Map.of("message",
                    "Account created successfully. You can now log in."));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // --- Admin endpoints ---

    @GetMapping("/api/admin/registrations")
    public ResponseEntity<List<RegistrationRequestResponse>> getAllRequests() {
        return ResponseEntity.ok(registrationService.getAllRequests());
    }

    @GetMapping("/api/admin/registrations/pending")
    public ResponseEntity<List<RegistrationRequestResponse>> getPendingRequests() {
        return ResponseEntity.ok(registrationService.getPendingRequests());
    }

    @PutMapping("/api/admin/registrations/{id}/approve")
    public ResponseEntity<?> approve(@PathVariable Long id, @RequestBody Map<String, String> body) {
        try {
            registrationService.approveRequest(id, body.get("role"));
            return ResponseEntity.ok(Map.of("message", "Registration approved and setup email sent"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/api/admin/registrations/{id}/reject")
    public ResponseEntity<?> reject(@PathVariable Long id) {
        try {
            registrationService.rejectRequest(id);
            return ResponseEntity.ok(Map.of("message", "Registration rejected"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
