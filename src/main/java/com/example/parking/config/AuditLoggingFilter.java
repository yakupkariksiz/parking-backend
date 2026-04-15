package com.example.parking.config;

import com.example.parking.service.AuditService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@Order(200)
public class AuditLoggingFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(AuditLoggingFilter.class);

    private final AuditService auditService;

    public AuditLoggingFilter(AuditService auditService) {
        this.auditService = auditService;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();

        return path.startsWith("/css/") ||
                path.startsWith("/js/") ||
                path.startsWith("/images/") ||
                path.startsWith("/webjars/") ||
                path.startsWith("/favicon") ||
                path.equals("/login") ||
                path.equals("/login.html") ||
                path.equals("/error");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        try {
            String path = request.getRequestURI();
            String method = request.getMethod();
            String action = "HTTP " + method + " " + path;

            var auth = SecurityContextHolder.getContext().getAuthentication();
            boolean isAuthenticatedUser = auth != null &&
                    auth.isAuthenticated() &&
                    !"anonymousUser".equals(auth.getName());

            if (isAuthenticatedUser) {
                auditService.audit(action, request);
            }
        } catch (Exception ex) {
            log.error("Audit logging failed for {} {}: {}", request.getMethod(), request.getRequestURI(), ex.getMessage(), ex);
        }

        filterChain.doFilter(request, response);
    }
}
