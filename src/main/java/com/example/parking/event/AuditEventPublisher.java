package com.example.parking.event;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Component
public class AuditEventPublisher {

    private final ApplicationEventPublisher publisher;

    public AuditEventPublisher(ApplicationEventPublisher publisher) {
        this.publisher = publisher;
    }

    /**
     * Publish an audit event, automatically resolving the current username from the
     * security context and the client IP from the current HTTP request.
     * Use this for all domain operations triggered by an already-authenticated user.
     */
    public void publish(String eventType, String action) {
        String username = resolveUsername();
        String clientIp = resolveClientIp();
        publisher.publishEvent(new AuditEvent(this, eventType, action, username, clientIp));
    }

    /**
     * Publish an audit event with explicit username and IP. Use this for login events
     * where the security context has not been populated yet.
     */
    public void publish(String eventType, String action, String username, HttpServletRequest request) {
        String clientIp = request != null ? request.getRemoteAddr() : null;
        publisher.publishEvent(new AuditEvent(this, eventType, action, username, clientIp));
    }

    private String resolveUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getName())) {
            return auth.getName();
        }
        return null;
    }

    private String resolveClientIp() {
        ServletRequestAttributes attrs =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attrs != null) {
            return attrs.getRequest().getRemoteAddr();
        }
        return null;
    }
}
