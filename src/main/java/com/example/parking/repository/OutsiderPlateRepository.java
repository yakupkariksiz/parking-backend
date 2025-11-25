package com.example.parking.repository;

import com.example.parking.model.OutsiderPlate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OutsiderPlateRepository extends JpaRepository<OutsiderPlate, Long> {
    Optional<OutsiderPlate> findByLicensePlate(String licensePlate);
}
