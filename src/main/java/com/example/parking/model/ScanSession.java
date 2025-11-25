package com.example.parking.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class ScanSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name; // örn: MORNING-2025-11-25-01

    @Column(nullable = false)
    private LocalDateTime createdAt;

    public ScanSession() {
    }

    public ScanSession(String name) {
        this.name = name;
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
