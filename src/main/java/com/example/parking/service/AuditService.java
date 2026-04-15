package com.example.parking.service;

import com.example.parking.model.AuditLog;
import com.example.parking.repository.AuditLogRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuditService {

    private final AuditLogRepository auditLogRepository;

    public AuditService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    @Transactional
    public void audit(String action, HttpServletRequest request) {
        String username = getCurrentUsername();
        String method = request.getMethod();
        String path = request.getRequestURI();
        // server.forward-headers-strategy=native already resolves the real IP
        String clientIp = request.getRemoteAddr();

        AuditLog log = new AuditLog(username, method, path, clientIp, action);
        auditLogRepository.save(log);
    }

    @Transactional(readOnly = true)
    public Page<AuditLog> listAll(Pageable pageable) {
        return auditLogRepository.findAll(pageable);
    }

    private String getCurrentUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getName())) {
            return auth.getName();
        }
        return null;
    }
}
