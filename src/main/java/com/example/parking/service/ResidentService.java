package com.example.parking.service;

import com.example.parking.dto.ResidentPlatesRequest;
import com.example.parking.dto.ResidentWithPlatesResponse;
import com.example.parking.model.Resident;
import com.example.parking.model.Vehicle;
import com.example.parking.repository.ResidentRepository;
import com.example.parking.repository.VehicleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

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
        Resident resident = residentRepository.findByUniqueCode(uniqueCode)
                .orElseGet(() -> residentRepository.save(new Resident(uniqueCode)));

        if (request.licensePlates() == null) {
            return;
        }

        // hedef plaka listesi (normalize + tekrarları temizle)
        Set<String> targetPlates = request.licensePlates().stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .map(String::toUpperCase)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toCollection(LinkedHashSet::new));

        // mevcut plakaları al
        List<Vehicle> currentVehicles = new ArrayList<>(resident.getVehicles());

        // Artık listede olmayan plakaları kaldır
        for (Vehicle v : currentVehicles) {
            if (!targetPlates.contains(v.getLicensePlate())) {
                resident.removeVehicle(v);
                vehicleRepository.delete(v);
            }
        }

        // Hedef listedeki her plaka için vehicle oluştur / reassignment
        for (String plate : targetPlates) {
            Vehicle vehicle = vehicleRepository.findByLicensePlate(plate)
                    .orElseGet(() -> new Vehicle(plate, resident));

            // farklı bir resident'a bağlıysa, bu residente ata
            vehicle.setResident(resident);
            vehicleRepository.save(vehicle);
        }
    }

    @Transactional(readOnly = true)
    public List<ResidentWithPlatesResponse> getAllResidentsWithPlates() {
        return residentRepository.findAll().stream()
                .map(r -> new ResidentWithPlatesResponse(
                        r.getId(),
                        r.getUniqueCode(),
                        r.getName(),
                        r.getAddress(),
                        r.getVehicles().stream()
                                .map(Vehicle::getLicensePlate)
                                .toList()
                ))
                .toList();
    }

    @Transactional
    public void deleteResident(Long residentId) {
        if (!residentRepository.existsById(residentId)) {
            return;
        }
        residentRepository.deleteById(residentId);
        // cascade + orphanRemoval sayesinde plakalar da silinecek
    }
}
