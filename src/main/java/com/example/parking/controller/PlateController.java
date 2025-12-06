package com.example.parking.controller;

import com.example.parking.model.Vehicle;
import com.example.parking.repository.OutsiderPlateRepository;
import com.example.parking.repository.VehicleRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RestController
@RequestMapping("/plates")
public class PlateController {

    private final VehicleRepository vehicleRepository;
    private final OutsiderPlateRepository outsiderPlateRepository;

    public PlateController(VehicleRepository vehicleRepository,
                           OutsiderPlateRepository outsiderPlateRepository) {
        this.vehicleRepository = vehicleRepository;
        this.outsiderPlateRepository = outsiderPlateRepository;
    }

    @GetMapping("/known")
    public List<String> getKnownPlates() {
        // Merge resident vehicle plates and outsider plates into a single, distinct, sorted list
        return Stream.concat(
                        vehicleRepository.findAll().stream()
                                .map(v -> v.getLicensePlate()),
                        outsiderPlateRepository.findAll().stream()
                                .map(op -> op.getLicensePlate())
                )
                .filter(plate -> plate != null && !plate.isBlank())
                .map(String::toUpperCase)
                .distinct()
                .sorted(Comparator.naturalOrder())
                .collect(Collectors.toList());
    }
}
