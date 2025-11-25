package com.example.parking.service;

import com.example.parking.dto.ScanEntryRequest;
import com.example.parking.model.ScanEntry;
import com.example.parking.model.ScanSession;
import com.example.parking.repository.ScanEntryRepository;
import com.example.parking.repository.ScanSessionRepository;
import com.example.parking.repository.VehicleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ScanEntryService {

    private final ScanEntryRepository scanEntryRepository;
    private final VehicleRepository vehicleRepository;
    private final ScanSessionRepository scanSessionRepository;
    private final OutsiderPlateService outsiderPlateService;

    public ScanEntryService(ScanEntryRepository scanEntryRepository,
                            VehicleRepository vehicleRepository,
                            ScanSessionRepository scanSessionRepository,
                            OutsiderPlateService outsiderPlateService) {
        this.scanEntryRepository = scanEntryRepository;
        this.vehicleRepository = vehicleRepository;
        this.scanSessionRepository = scanSessionRepository;
        this.outsiderPlateService = outsiderPlateService;
    }


    @Transactional
    public void createScanEntry(ScanEntryRequest request) {
        String normalizedPlate = request.licensePlate()
                .trim()
                .toUpperCase();

        String location = request.location().trim();

        ScanSession session = null;
        if (request.sessionName() != null && !request.sessionName().isBlank()) {
            String sessionName = request.sessionName().trim();
            session = scanSessionRepository.findByName(sessionName)
                    .orElseGet(() -> scanSessionRepository.save(new ScanSession(sessionName)));
        }

        ScanEntry entry = new ScanEntry(normalizedPlate, location);
        entry.setSession(session);

        boolean isResident = vehicleRepository
                .findByLicensePlate(normalizedPlate)
                .isPresent();

        entry.setResident(isResident);

        if (!isResident) {
            outsiderPlateService.registerOutsider(normalizedPlate);
        }

        scanEntryRepository.save(entry);
    }
}
