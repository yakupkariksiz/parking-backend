package com.example.parking.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "registration_request")
public class RegistrationRequest {

    public enum Status {
        PENDING, APPROVED, REJECTED
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Status status = Status.PENDING;

    @Column(length = 20)
    private String approvedRole; // ROLE_USER or ROLE_ADMIN, set on approval

    @Column(unique = true)
    private String token; // UUID for password-set link

    private LocalDateTime tokenExpiry;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public RegistrationRequest() {
    }

    public RegistrationRequest(String email) {
        this.email = email;
        this.createdAt = LocalDateTime.now();
        this.status = Status.PENDING;
    }

    public Long getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public String getApprovedRole() {
        return approvedRole;
    }

    public void setApprovedRole(String approvedRole) {
        this.approvedRole = approvedRole;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public LocalDateTime getTokenExpiry() {
        return tokenExpiry;
    }

    public void setTokenExpiry(LocalDateTime tokenExpiry) {
        this.tokenExpiry = tokenExpiry;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
