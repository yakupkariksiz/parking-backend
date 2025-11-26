package com.example.parking.repository;

import com.example.parking.model.LocationDefinition;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LocationDefinitionRepository extends JpaRepository<LocationDefinition, Long> {
}
