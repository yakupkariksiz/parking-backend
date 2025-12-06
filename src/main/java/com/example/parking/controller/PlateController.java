package com.example.parking.controller;

import com.example.parking.model.Vehicle;
import com.example.parking.repository.OutsiderPlateRepository;
import com.example.parking.repository.VehicleRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
    public Map<String, List<String>> getKnownPlates() {
        // Separate resident vehicle plates and outsider plates, then return both lists
        List<String> residentPlates = vehicleRepository.findAll().stream()
                .map(v -> v.getLicensePlate())
                .filter(plate -> plate != null && !plate.isBlank())
                .map(String::toUpperCase)
                .distinct()
                .sorted(Comparator.naturalOrder())
                .collect(Collectors.toList());

        List<String> outsiderPlates = outsiderPlateRepository.findAll().stream()
                .map(op -> op.getLicensePlate())
                .filter(plate -> plate != null && !plate.isBlank())
                .map(String::toUpperCase)
                .distinct()
                .sorted(Comparator.naturalOrder())
                .collect(Collectors.toList());

        Map<String, List<String>> result = new HashMap<>();
        result.put("residents", residentPlates);
        result.put("outsiders", outsiderPlates);
        return result;
    }
}
