package com.example.parking.service;

import com.example.parking.dto.ResidentPlatesRequest;
import com.example.parking.model.Resident;
import com.example.parking.model.Vehicle;
import com.example.parking.repository.ResidentRepository;
import com.example.parking.repository.VehicleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ResidentService {

    private final ResidentRepository residentRepository;
    private final VehicleRepository vehicleRepository;

    public ResidentService(ResidentRepository residentRepository,
                           VehicleRepository vehicleRepository) {
        this.residentRepository = residentRepository;
        this.vehicleRepository = vehicleRepository;
    }

    @Transactional
    public void addOrUpdatePlatesForResident(String uniqueCode, ResidentPlatesRequest request) {
        // find or create resident
        Resident resident = residentRepository.findByUniqueCode(uniqueCode)
                .orElseGet(() -> {
                    Resident r = new Resident(uniqueCode);
                    return residentRepository.save(r);
                });

        if (request.licensePlates() == null) {
            return;
        }

        for (String plateRaw : request.licensePlates()) {
            if (plateRaw == null || plateRaw.isBlank()) continue;

            String plate = plateRaw.trim().toUpperCase();

            Vehicle vehicle = vehicleRepository.findByLicensePlate(plate)
                    .orElseGet(() -> new Vehicle(plate, resident));

            // if vehicle existed but was linked to another resident, reassign
            vehicle.setResident(resident);
            vehicleRepository.save(vehicle);
        }
    }
}
