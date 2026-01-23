package com.example.parking.service;

import com.example.parking.dto.ResidentPlatesRequest;
import com.example.parking.dto.ResidentWithPlatesResponse;
import com.example.parking.model.Resident;
import com.example.parking.model.Vehicle;
import com.example.parking.repository.ResidentRepository;
import com.example.parking.repository.VehicleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class ResidentService {

    private static final Logger log = LoggerFactory.getLogger(ResidentService.class);

    private final ResidentRepository residentRepository;
    private final VehicleRepository vehicleRepository;

    public ResidentService(ResidentRepository residentRepository,
                           VehicleRepository vehicleRepository) {
        this.residentRepository = residentRepository;
        this.vehicleRepository = vehicleRepository;
    }

    @Transactional
    public void addOrUpdatePlatesForResident(String uniqueCode, ResidentPlatesRequest request) {
        log.info("Adding/updating plates for resident: {}", uniqueCode);

        Resident resident = residentRepository.findByUniqueCode(uniqueCode)
                .orElseGet(() -> {
                    log.info("Creating new resident: {}", uniqueCode);
                    return residentRepository.save(new Resident(uniqueCode));
                });

        if (request.licensePlates() == null) {
            log.warn("No license plates provided for resident: {}", uniqueCode);
            return;
        }

        // hedef plaka listesi (normalize + tekrarları temizle)
        Set<String> targetPlates = request.licensePlates().stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .map(String::toUpperCase)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toCollection(LinkedHashSet::new));

        log.info("Target plates for resident {}: {}", uniqueCode, targetPlates);

        // mevcut plakaları al
        List<Vehicle> currentVehicles = new ArrayList<>(resident.getVehicles());

        // Artık listede olmayan plakaları kaldır
        for (Vehicle v : currentVehicles) {
            if (!targetPlates.contains(v.getLicensePlate())) {
                log.info("Removing vehicle {} from resident {}", v.getLicensePlate(), uniqueCode);
                resident.removeVehicle(v);
                vehicleRepository.delete(v);
            }
        }

        // Hedef listedeki her plaka için vehicle oluştur / reassignment
        for (String plate : targetPlates) {
            Optional<Vehicle> existingVehicle = vehicleRepository.findByLicensePlate(plate);

            if (existingVehicle.isPresent()) {
                Vehicle vehicle = existingVehicle.get();
                log.info("Found existing vehicle: {} (currently assigned to resident ID: {})",
                    plate, vehicle.getResident() != null ? vehicle.getResident().getId() : "none");

                // Eğer farklı bir resident'a bağlıysa, eski resident'tan kaldır
                if (vehicle.getResident() != null && !vehicle.getResident().equals(resident)) {
                    log.info("Reassigning vehicle {} from resident {} to resident {}",
                        plate, vehicle.getResident().getUniqueCode(), uniqueCode);
                    vehicle.getResident().removeVehicle(vehicle);
                }
                // Bu resident'a ata
                if (!resident.getVehicles().contains(vehicle)) {
                    resident.addVehicle(vehicle);
                    log.info("Added vehicle {} to resident {}", plate, uniqueCode);
                }
            } else {
                // Yeni vehicle oluştur ve resident'a ekle
                log.info("Creating new vehicle: {} for resident {}", plate, uniqueCode);
                Vehicle newVehicle = new Vehicle(plate, resident);
                vehicleRepository.save(newVehicle);
                if (!resident.getVehicles().contains(newVehicle)) {
                    resident.getVehicles().add(newVehicle);
                }
            }
        }

        // Save resident to persist the relationship
        residentRepository.save(resident);
        log.info("Successfully saved resident {} with {} vehicles", uniqueCode, resident.getVehicles().size());
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

    @Transactional
    public void deleteResidents(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return;
        }
        ids.forEach(residentRepository::deleteById);
    }

}
