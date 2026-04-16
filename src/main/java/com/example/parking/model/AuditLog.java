package com.example.parking.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "audit_log")
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 50, nullable = false)
    private String eventType;

    @Column(length = 64)
    private String username;

    @Column(length = 512)
    private String action;

    @Column(length = 64)
    private String clientIp;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    public AuditLog() {
    }

    public AuditLog(String eventType, String username, String action, String clientIp) {
        this.eventType = eventType;
        this.username = username;
        this.action = action;
        this.clientIp = clientIp;
        this.timestamp = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public String getEventType() {
        return eventType;
    }

    public String getUsername() {
        return username;
    }

    public String getAction() {
        return action;
    }

    public String getClientIp() {
        return clientIp;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }
}
