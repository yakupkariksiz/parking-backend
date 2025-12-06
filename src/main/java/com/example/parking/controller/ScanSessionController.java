package com.example.parking.controller;

import com.example.parking.dto.ScanSessionSummary;
import com.example.parking.model.ScanSession;
import com.example.parking.repository.ScanSessionRepository;
import com.example.parking.service.ScanSessionService;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;

@RestController
@RequestMapping("/scan-sessions")
public class ScanSessionController {

    private final ScanSessionRepository scanSessionRepository;
    private final ScanSessionService scanSessionService;
    private final DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    public ScanSessionController(ScanSessionRepository scanSessionRepository,
                                 ScanSessionService scanSessionService) {
        this.scanSessionRepository = scanSessionRepository;
        this.scanSessionService = scanSessionService;
    }

    @GetMapping
    public List<ScanSessionSummary> listSessions() {
        return scanSessionRepository.findAll().stream()
                .sorted(Comparator.comparing(ScanSession::getCreatedAt).reversed())
                .map(s -> new ScanSessionSummary(
                        s.getId(),
                        s.getName(),
                        s.getCreatedAt().format(formatter)
                ))
                .toList();
    }

    @DeleteMapping("/{id}")
    public void deleteSession(@PathVariable("id") Long id) {
        scanSessionService.deleteSessionWithEntries(id);
    }
}
