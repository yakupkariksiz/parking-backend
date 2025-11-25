package com.example.parking.repository;

import com.example.parking.model.ScanSession;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ScanSessionRepository extends JpaRepository<ScanSession, Long> {
    Optional<ScanSession> findByName(String name);
}
