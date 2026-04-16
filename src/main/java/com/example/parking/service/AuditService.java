package com.example.parking.service;

import com.example.parking.model.AuditLog;
import com.example.parking.repository.AuditLogRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuditService {

    private final AuditLogRepository auditLogRepository;

    public AuditService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    @Transactional(readOnly = true)
    public Page<AuditLog> listAll(Pageable pageable) {
        return auditLogRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public Page<AuditLog> listByEventType(String eventType, Pageable pageable) {
        return auditLogRepository.findByEventType(eventType, pageable);
    }

    @Transactional(readOnly = true)
    public Page<AuditLog> listByUsername(String username, Pageable pageable) {
        return auditLogRepository.findByUsernameContainingIgnoreCase(username, pageable);
    }

    @Transactional(readOnly = true)
    public Page<AuditLog> listByEventTypeAndUsername(String eventType, String username, Pageable pageable) {
        return auditLogRepository.findByEventTypeAndUsernameContainingIgnoreCase(eventType, username, pageable);
    }
}
