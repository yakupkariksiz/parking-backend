package com.example.parking.controller;

import com.example.parking.model.AuditLog;
import com.example.parking.service.AuditService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/audit")
public class AuditController {

    private final AuditService auditService;

    public AuditController(AuditService auditService) {
        this.auditService = auditService;
    }

    @GetMapping
    public ResponseEntity<?> listAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size,
            @RequestParam(required = false) String eventType,
            @RequestParam(required = false) String username) {
        try {
            size = Math.min(size, 200);
            Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "timestamp"));

            boolean hasEventType = eventType != null && !eventType.isBlank();
            boolean hasUsername = username != null && !username.isBlank();

            Page<AuditLog> result;
            if (hasEventType && hasUsername) {
                result = auditService.listByEventTypeAndUsername(eventType.trim(), username.trim(), pageable);
            } else if (hasEventType) {
                result = auditService.listByEventType(eventType.trim(), pageable);
            } else if (hasUsername) {
                result = auditService.listByUsername(username.trim(), pageable);
            } else {
                result = auditService.listAll(pageable);
            }

            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }
}
