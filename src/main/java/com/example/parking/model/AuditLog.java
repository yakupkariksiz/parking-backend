package com.example.parking.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "audit_log")
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Hangi kullanıcı (null olabilir, anonymous / login öncesi)
    @Column(length = 64)
    private String username;

    // HTTP method: GET, POST, ...
    @Column(length = 10, nullable = false)
    private String httpMethod;

    // İstek path'i: /residents.html, /scan-entries, ...
    @Column(length = 255, nullable = false)
    private String path;

    // İstemci IP (best effort)
    @Column(length = 64)
    private String clientIp;

    // Basit açıklama veya action bilgisi
    @Column(length = 512)
    private String action;

    // İsteğin zamanı
    @Column(nullable = false)
    private LocalDateTime timestamp;

    public AuditLog() {
    }

    public AuditLog(String username, String httpMethod, String path, String clientIp, String action) {
        this.username = username;
        this.httpMethod = httpMethod;
        this.path = path;
        this.clientIp = clientIp;
        this.action = action;
        this.timestamp = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getHttpMethod() {
        return httpMethod;
    }

    public String getPath() {
        return path;
    }

    public String getClientIp() {
        return clientIp;
    }

    public String getAction() {
        return action;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }
}
