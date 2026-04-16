package com.example.parking.service;

import com.example.parking.dto.BulkImportResultResponse;
import com.example.parking.dto.BulkImportRowResult;
import com.example.parking.dto.ResidentPlatesRequest;
import com.example.parking.dto.ResidentWithPlatesResponse;
import com.example.parking.event.AuditEventPublisher;
import com.example.parking.event.EventType;
import com.example.parking.model.Resident;
import com.example.parking.model.Vehicle;
import com.example.parking.repository.ResidentRepository;
import com.example.parking.repository.VehicleRepository;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class ResidentService {

    private static final Logger log = LoggerFactory.getLogger(ResidentService.class);

    private final ResidentRepository residentRepository;
    private final VehicleRepository vehicleRepository;
    private final AuditEventPublisher auditEventPublisher;

    public ResidentService(ResidentRepository residentRepository,
                           VehicleRepository vehicleRepository,
                           AuditEventPublisher auditEventPublisher) {
        this.residentRepository = residentRepository;
        this.vehicleRepository = vehicleRepository;
        this.auditEventPublisher = auditEventPublisher;
    }

    @Transactional
    public void addOrUpdatePlatesForResident(String uniqueCode, ResidentPlatesRequest request) {
        log.info("Adding/updating plates for resident: {}", uniqueCode);

        boolean[] isNew = {false};
        Resident resident = residentRepository.findByUniqueCode(uniqueCode)
                .orElseGet(() -> {
                    log.info("Creating new resident: {}", uniqueCode);
                    isNew[0] = true;
                    return residentRepository.save(new Resident(uniqueCode));
                });

        if (request.licensePlates() == null) {
            log.warn("No license plates provided for resident: {}", uniqueCode);
            return;
        }

        Set<String> targetPlates = request.licensePlates().stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .map(String::toUpperCase)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toCollection(LinkedHashSet::new));

        log.info("Target plates for resident {}: {}", uniqueCode, targetPlates);

        List<Vehicle> currentVehicles = new ArrayList<>(resident.getVehicles());

        for (Vehicle v : currentVehicles) {
            if (!targetPlates.contains(v.getLicensePlate())) {
                log.info("Removing vehicle {} from resident {}", v.getLicensePlate(), uniqueCode);
                resident.removeVehicle(v);
                vehicleRepository.delete(v);
            }
        }

        for (String plate : targetPlates) {
            Optional<Vehicle> existingVehicle = vehicleRepository.findByLicensePlate(plate);

            if (existingVehicle.isPresent()) {
                Vehicle vehicle = existingVehicle.get();
                log.info("Found existing vehicle: {} (currently assigned to resident ID: {})",
                    plate, vehicle.getResident() != null ? vehicle.getResident().getId() : "none");

                if (vehicle.getResident() != null && !vehicle.getResident().equals(resident)) {
                    log.info("Reassigning vehicle {} from resident {} to resident {}",
                        plate, vehicle.getResident().getUniqueCode(), uniqueCode);
                    vehicle.getResident().removeVehicle(vehicle);
                }
                if (!resident.getVehicles().contains(vehicle)) {
                    resident.addVehicle(vehicle);
                    log.info("Added vehicle {} to resident {}", plate, uniqueCode);
                }
            } else {
                log.info("Creating new vehicle: {} for resident {}", plate, uniqueCode);
                Vehicle newVehicle = new Vehicle(plate, resident);
                vehicleRepository.save(newVehicle);
                if (!resident.getVehicles().contains(newVehicle)) {
                    resident.getVehicles().add(newVehicle);
                }
            }
        }

        residentRepository.save(resident);
        log.info("Successfully saved resident {} with {} vehicles", uniqueCode, resident.getVehicles().size());

        String eventType = isNew[0] ? EventType.RESIDENT_CREATE : EventType.RESIDENT_UPDATE;
        String action = (isNew[0] ? "Created resident " : "Updated plates for resident ")
                + uniqueCode + " — plates: " + targetPlates;
        auditEventPublisher.publish(eventType, action);
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
        residentRepository.findById(residentId).ifPresent(resident -> {
            String uniqueCode = resident.getUniqueCode();
            residentRepository.deleteById(residentId);
            auditEventPublisher.publish(EventType.RESIDENT_DELETE,
                    "Deleted resident " + uniqueCode);
        });
    }

    @Transactional
    public void deleteResidents(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return;
        }
        List<String> deletedCodes = new ArrayList<>();
        for (Long id : ids) {
            residentRepository.findById(id).ifPresent(resident -> {
                deletedCodes.add(resident.getUniqueCode());
                residentRepository.deleteById(id);
            });
        }
        if (!deletedCodes.isEmpty()) {
            auditEventPublisher.publish(EventType.RESIDENT_DELETE,
                    "Bulk deleted residents: " + deletedCodes);
        }
    }

    private static final Pattern PLATE_PATTERN = Pattern.compile("^[A-Z0-9]{2,10}$");

    @Transactional
    public BulkImportResultResponse bulkImportFromFile(MultipartFile file) throws IOException {
        String filename = file.getOriginalFilename() != null ? file.getOriginalFilename().toLowerCase() : "";
        List<String[]> rawRows;

        if (filename.endsWith(".xlsx")) {
            rawRows = parseExcel(file);
        } else if (filename.endsWith(".csv")) {
            rawRows = parseCsv(file);
        } else {
            throw new IllegalArgumentException("Unsupported file type. Please upload a .csv or .xlsx file.");
        }

        List<BulkImportRowResult> rowResults = new ArrayList<>();
        // uniqueCode -> set of new plates to add
        Map<String, Set<String>> toImport = new LinkedHashMap<>();

        for (int i = 0; i < rawRows.size(); i++) {
            int rowNumber = i + 1;
            String[] cols = rawRows.get(i);

            if (cols.length < 2) {
                rowResults.add(new BulkImportRowResult(rowNumber, "", "", false,
                        "Row has fewer than 2 columns"));
                continue;
            }

            String uniqueCode = cols[0].trim();
            String plate = cols[1].trim().toUpperCase();

            if (uniqueCode.isEmpty()) {
                rowResults.add(new BulkImportRowResult(rowNumber, uniqueCode, plate, false,
                        "Household reference (unique code) is empty"));
                continue;
            }

            if (plate.isEmpty()) {
                rowResults.add(new BulkImportRowResult(rowNumber, uniqueCode, plate, false,
                        "License plate is empty"));
                continue;
            }

            if (!PLATE_PATTERN.matcher(plate).matches()) {
                rowResults.add(new BulkImportRowResult(rowNumber, uniqueCode, plate, false,
                        "Invalid license plate format (allowed: 2-10 alphanumeric characters)"));
                continue;
            }

            toImport.computeIfAbsent(uniqueCode, k -> new LinkedHashSet<>()).add(plate);
            rowResults.add(new BulkImportRowResult(rowNumber, uniqueCode, plate, true, null));
        }

        // For each uniqueCode, merge new plates with existing ones and upsert
        for (Map.Entry<String, Set<String>> entry : toImport.entrySet()) {
            String uniqueCode = entry.getKey();
            Set<String> newPlates = entry.getValue();

            Set<String> merged = new LinkedHashSet<>();
            residentRepository.findByUniqueCode(uniqueCode).ifPresent(r ->
                    r.getVehicles().forEach(v -> merged.add(v.getLicensePlate())));
            merged.addAll(newPlates);

            addOrUpdatePlatesForResident(uniqueCode, new ResidentPlatesRequest(new ArrayList<>(merged)));
        }

        long successCount = rowResults.stream().filter(BulkImportRowResult::success).count();
        long failedCount = rowResults.stream().filter(r -> !r.success()).count();

        log.info("Bulk import complete — {} rows processed, {} succeeded, {} failed",
                rowResults.size(), successCount, failedCount);

        auditEventPublisher.publish(EventType.RESIDENT_UPDATE,
                "Bulk import: " + successCount + " plates imported for "
                + toImport.size() + " residents");

        return new BulkImportResultResponse(
                rowResults.size(),
                (int) successCount,
                (int) failedCount,
                rowResults
        );
    }

    private List<String[]> parseExcel(MultipartFile file) throws IOException {
        List<String[]> rows = new ArrayList<>();
        try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            boolean firstRow = true;
            for (Row row : sheet) {
                if (firstRow) {
                    firstRow = false;
                    // Skip header row if first cell looks like a header
                    String firstCell = getCellStringValue(row.getCell(0));
                    if (firstCell.equalsIgnoreCase("uniqueCode")
                            || firstCell.equalsIgnoreCase("unique_code")
                            || firstCell.equalsIgnoreCase("code")) {
                        continue;
                    }
                }
                String col0 = getCellStringValue(row.getCell(0));
                String col1 = getCellStringValue(row.getCell(1));
                if (col0.isEmpty() && col1.isEmpty()) {
                    continue;
                }
                rows.add(new String[]{col0, col1});
            }
        }
        return rows;
    }

    private String getCellStringValue(Cell cell) {
        if (cell == null) return "";
        if (cell.getCellType() == CellType.NUMERIC) {
            return String.valueOf((long) cell.getNumericCellValue());
        }
        return cell.toString().trim();
    }

    private List<String[]> parseCsv(MultipartFile file) throws IOException {
        List<String[]> rows = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            boolean firstLine = true;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;
                String[] cols = line.split(",", -1);
                if (firstLine) {
                    firstLine = false;
                    String firstCol = cols[0].trim().toLowerCase();
                    if (firstCol.equals("uniquecode") || firstCol.equals("unique_code")
                            || firstCol.equals("code")) {
                        continue;
                    }
                }
                rows.add(cols);
            }
        }
        return rows;
    }
}
