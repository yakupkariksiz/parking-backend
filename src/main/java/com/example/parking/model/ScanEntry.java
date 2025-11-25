package com.example.parking.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class ScanEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String licensePlate;

    @Column(nullable = false)
    private String location;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    // diğer alanların arasına ekle
    @ManyToOne
    private ScanSession session;

    // Optional: will be used later for resident matching
    private Boolean resident;

    public ScanEntry() {
    }

    public ScanEntry(String licensePlate, String location) {
        this.licensePlate = licensePlate;
        this.location = location;
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public String getLicensePlate() {
        return licensePlate;
    }

    public void setLicensePlate(String licensePlate) {
        this.licensePlate = licensePlate;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public Boolean getResident() {
        return resident;
    }

    public void setResident(Boolean resident) {
        this.resident = resident;
    }

    public ScanSession getSession() {
        return session;
    }

    public void setSession(ScanSession session) {
        this.session = session;
    }
}
