package com.example.parking.model;

import jakarta.persistence.*;

@Entity
@Table(name = "app_user") // "user" reserved olabilir, o yüzden app_user
public class AppUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 64)
    private String username;

    @Column(nullable = true) // nullable for OAuth2 users
    private String password; // BCRYPT encoded (null for OAuth2 users)

    @Column(nullable = false, length = 32)
    private String role;     // "ROLE_ADMIN" veya "ROLE_USER"

    @Column(nullable = false)
    private boolean enabled = true;

    @Column(unique = true)
    private String email;    // For OAuth2 users

    @Column(length = 32)
    private String provider; // "google", "local", etc.

    @Column
    private String providerId; // OAuth2 provider's user ID

    public AppUser() {
    }

    public AppUser(String username, String password, String role, boolean enabled) {
        this.username = username;
        this.password = password;
        this.role = role;
        this.enabled = enabled;
        this.provider = "local";
    }

    // Constructor for OAuth2 users
    public AppUser(String username, String email, String role, String provider, String providerId) {
        this.username = username;
        this.email = email;
        this.role = role;
        this.enabled = true;
        this.provider = provider;
        this.providerId = providerId;
    }

    public Long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public String getProviderId() {
        return providerId;
    }

    public void setProviderId(String providerId) {
        this.providerId = providerId;
    }
}
