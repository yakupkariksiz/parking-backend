package com.example.parking.repository;

import com.example.parking.model.ScanEntry;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ScanEntryRepository extends JpaRepository<ScanEntry, Long> {

    List<ScanEntry> findBySessionId(Long sessionId);
}
