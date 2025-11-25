package com.example.parking.controller;

import com.example.parking.model.Vehicle;
import com.example.parking.repository.VehicleRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/plates")
public class PlateController {

    private final VehicleRepository vehicleRepository;

    public PlateController(VehicleRepository vehicleRepository) {
        this.vehicleRepository = vehicleRepository;
    }

    @GetMapping("/known")
    public List<String> getKnownPlates() {
        return vehicleRepository.findAll().stream()
                .map(v -> v.getLicensePlate().toUpperCase())
                .distinct()
                .sorted(Comparator.naturalOrder())
                .collect(Collectors.toList());
    }
}
