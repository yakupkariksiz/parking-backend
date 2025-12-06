package com.example.parking.service;

import com.example.parking.repository.ScanEntryRepository;
import com.example.parking.repository.ScanSessionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ScanSessionService {

    private final ScanSessionRepository scanSessionRepository;
    private final ScanEntryRepository scanEntryRepository;

    public ScanSessionService(ScanSessionRepository scanSessionRepository,
                              ScanEntryRepository scanEntryRepository) {
        this.scanSessionRepository = scanSessionRepository;
        this.scanEntryRepository = scanEntryRepository;
    }

    @Transactional
    public void deleteSessionWithEntries(Long sessionId) {
        // First delete scan entries referencing this session, then the session itself
        scanEntryRepository.findBySessionId(sessionId).forEach(scanEntryRepository::delete);
        scanSessionRepository.deleteById(sessionId);
    }
}

