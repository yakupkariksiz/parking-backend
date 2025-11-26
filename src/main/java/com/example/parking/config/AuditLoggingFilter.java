package com.example.parking.config;

import com.example.parking.service.AuditService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class AuditLoggingFilter extends OncePerRequestFilter {

    private final AuditService auditService;

    public AuditLoggingFilter(AuditService auditService) {
        this.auditService = auditService;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();

        // Static ve bazı hassas endpoint'leri loglama:
        if (path.startsWith("/css/") ||
                path.startsWith("/js/") ||
                path.startsWith("/images/") ||
                path.startsWith("/webjars/") ||
                path.startsWith("/favicon") ||
                path.equals("/login") ||
                path.equals("/login.html") ||
                path.equals("/error")) {
            return true;
        }

        // İstersen actuator vs varsa onları da ekleyebilirsin
        return false;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        try {
            String path = request.getRequestURI();
            String method = request.getMethod();

            // Basit otomatik action tanımı
            String action = "HTTP " + method + " " + path;

            // Burada sadece authenticated user'ları loglamak isteyebilirsin:
            var auth = SecurityContextHolder.getContext().getAuthentication();
            boolean isAuthenticatedUser = auth != null &&
                    auth.isAuthenticated() &&
                    !"anonymousUser".equals(auth.getName());

            if (isAuthenticatedUser) {
                auditService.audit(action, request);
            }
        } catch (Exception ex) {
            // Audit fail olsa bile ana isteği bozmayalım
            // log vs atmak istersen buraya koyabilirsin
        }

        filterChain.doFilter(request, response);
    }
}
