package com.example.parking.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class OutsiderPlate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String licensePlate;

    @Column(nullable = false)
    private LocalDateTime firstSeen;

    @Column(nullable = false)
    private LocalDateTime lastSeen;

    @Column(nullable = false)
    private long scanCount;

    public OutsiderPlate() {
    }

    public OutsiderPlate(String licensePlate) {
        this.licensePlate = licensePlate;
        this.firstSeen = LocalDateTime.now();
        this.lastSeen = this.firstSeen;
        this.scanCount = 1;
    }

    public Long getId() {
        return id;
    }

    public String getLicensePlate() {
        return licensePlate;
    }

    public LocalDateTime getFirstSeen() {
        return firstSeen;
    }

    public LocalDateTime getLastSeen() {
        return lastSeen;
    }

    public long getScanCount() {
        return scanCount;
    }

    public void seenAgain() {
        this.lastSeen = LocalDateTime.now();
        this.scanCount++;
    }
}
