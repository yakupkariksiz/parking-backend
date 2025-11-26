package com.example.parking.controller;

import com.example.parking.model.LocationDefinition;
import com.example.parking.repository.LocationDefinitionRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Comparator;
import java.util.List;

@RestController
@RequestMapping("/locations")
public class LocationController {

    private final LocationDefinitionRepository locationRepo;

    public LocationController(LocationDefinitionRepository locationRepo) {
        this.locationRepo = locationRepo;
    }

    @GetMapping("/known")
    public List<String> getKnownLocations() {
        return locationRepo.findAll().stream()
                .sorted(Comparator
                        .comparing(LocationDefinition::getArea)
                        .thenComparing(LocationDefinition::getSpotNumber))
                .map(LocationDefinition::getCode) // Örn: "NORTH-01"
                .toList();
    }
}
