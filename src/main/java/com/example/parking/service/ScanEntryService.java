package com.example.parking.service;

import com.example.parking.dto.ScanEntryRequest;
import com.example.parking.model.ScanEntry;
import com.example.parking.model.ScanSession;
import com.example.parking.repository.ScanEntryRepository;
import com.example.parking.repository.ScanSessionRepository;
import com.example.parking.repository.VehicleRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ScanEntryService {

    private static final Logger log = LoggerFactory.getLogger(ScanEntryService.class);

    private final ScanEntryRepository scanEntryRepository;
    private final VehicleRepository vehicleRepository;
    private final ScanSessionRepository scanSessionRepository;
    private final OutsiderPlateService outsiderPlateService;
    private final AuditService auditService;

    public ScanEntryService(ScanEntryRepository scanEntryRepository,
                            VehicleRepository vehicleRepository,
                            ScanSessionRepository scanSessionRepository,
                            OutsiderPlateService outsiderPlateService,
                            AuditService auditService) {
        this.scanEntryRepository = scanEntryRepository;
        this.vehicleRepository = vehicleRepository;
        this.scanSessionRepository = scanSessionRepository;
        this.outsiderPlateService = outsiderPlateService;
        this.auditService = auditService;
    }

    @Transactional
    public void createScanEntry(ScanEntryRequest request, HttpServletRequest httpRequest) {
        String normalizedPlate = request.licensePlate()
                .trim()
                .toUpperCase();

        log.info("Creating scan entry for license plate: {}", normalizedPlate);

        String location = request.location().trim();
        if (request.noParking() != null && request.noParking()) {
            location = "NO_PARKING";
        }

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

        log.info("License plate {} lookup result: isResident={}", normalizedPlate, isResident);

        entry.setResident(isResident);

        if (!isResident) {
            log.info("Registering {} as outsider plate", normalizedPlate);
            outsiderPlateService.registerOutsider(normalizedPlate);
        }

        scanEntryRepository.save(entry);

        // entry kaydedildikten hemen sonra:
        String action = "Scanned plate " + normalizedPlate + " at location " + location
                + (isResident ? " (resident)" : " (outsider)");
        auditService.audit(action, httpRequest);

        log.info("Successfully created scan entry for {} at location {}", normalizedPlate, location);
    }
}
