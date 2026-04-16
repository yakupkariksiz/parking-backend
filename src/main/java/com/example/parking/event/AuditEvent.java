package com.example.parking.event;

import org.springframework.context.ApplicationEvent;

public class AuditEvent extends ApplicationEvent {

    private final String eventType;
    private final String action;
    private final String username;
    private final String clientIp;

    public AuditEvent(Object source, String eventType, String action, String username, String clientIp) {
        super(source);
        this.eventType = eventType;
        this.action = action;
        this.username = username;
        this.clientIp = clientIp;
    }

    public String getEventType() {
        return eventType;
    }

    public String getAction() {
        return action;
    }

    public String getUsername() {
        return username;
    }

    public String getClientIp() {
        return clientIp;
    }
}
