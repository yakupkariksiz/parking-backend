package com.example.parking.service;

import com.example.parking.dto.SessionStatsResponse;
import com.example.parking.model.ScanEntry;
import com.example.parking.model.ScanSession;
import com.example.parking.repository.ScanEntryRepository;
import com.example.parking.repository.ScanSessionRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class StatsService {

    private final ScanEntryRepository scanEntryRepository;
    private final ScanSessionRepository scanSessionRepository;

    public StatsService(ScanEntryRepository scanEntryRepository,
                        ScanSessionRepository scanSessionRepository) {
        this.scanEntryRepository = scanEntryRepository;
        this.scanSessionRepository = scanSessionRepository;
    }

    public SessionStatsResponse getSessionStats(Long sessionId) {
        ScanSession session = scanSessionRepository.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("Session not found: " + sessionId));

        List<ScanEntry> entries = scanEntryRepository.findBySessionId(sessionId);

        long total = entries.size();
        if (total == 0) {
            return new SessionStatsResponse(session.getId(), session.getName(),
                    0, 0, 0, 0.0, 0.0);
        }

        long residentCount = entries.stream()
                .filter(e -> Boolean.TRUE.equals(e.getResident()))
                .count();

        long nonResidentCount = total - residentCount;

        double residentPct = round(residentCount * 100.0 / total);
        double nonResidentPct = round(nonResidentCount * 100.0 / total);

        return new SessionStatsResponse(
                session.getId(),
                session.getName(),
                total,
                residentCount,
                nonResidentCount,
                residentPct,
                nonResidentPct
        );
    }

    private double round(double value) {
        return Math.round(value * 10.0) / 10.0; // 1 decimal
    }
}
