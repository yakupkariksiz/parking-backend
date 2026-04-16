package com.example.parking.event;

import com.example.parking.model.AuditLog;
import com.example.parking.repository.AuditLogRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class AuditEventListener {

    private static final Logger log = LoggerFactory.getLogger(AuditEventListener.class);

    private final AuditLogRepository auditLogRepository;

    public AuditEventListener(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    /**
     * Persists an audit log entry after the publishing transaction commits.
     * fallbackExecution = true ensures login events (which run outside a transaction)
     * are also persisted.
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void onAuditEvent(AuditEvent event) {
        try {
            AuditLog entry = new AuditLog(
                    event.getEventType(),
                    event.getUsername(),
                    event.getAction(),
                    event.getClientIp()
            );
            auditLogRepository.save(entry);
        } catch (Exception ex) {
            log.error("Failed to persist audit event [{}]: {}", event.getEventType(), ex.getMessage(), ex);
        }
    }
}
