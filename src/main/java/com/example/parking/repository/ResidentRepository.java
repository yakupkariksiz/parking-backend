package com.example.parking.repository;

import com.example.parking.model.Resident;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ResidentRepository extends JpaRepository<Resident, Long> {
    Optional<Resident> findByUniqueCode(String uniqueCode);
}
