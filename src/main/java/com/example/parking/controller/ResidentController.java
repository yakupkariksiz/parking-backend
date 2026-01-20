package com.example.parking.controller;

import com.example.parking.dto.ResidentBulkDeleteRequest;
import com.example.parking.dto.ResidentPlatesRequest;
import com.example.parking.dto.ResidentWithPlatesResponse;
import com.example.parking.service.ExcelExportService;
import com.example.parking.service.ResidentService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@RestController
@RequestMapping("/residents")
public class ResidentController {

    private final ResidentService residentService;
    private final ExcelExportService excelExportService;

    public ResidentController(ResidentService residentService, ExcelExportService excelExportService) {
        this.residentService = residentService;
        this.excelExportService = excelExportService;
    }

    @PostMapping("/{uniqueCode}/plates")
    public ResponseEntity<Void> addPlates(
            @PathVariable String uniqueCode,
            @RequestBody ResidentPlatesRequest request
    ) {
        residentService.addOrUpdatePlatesForResident(uniqueCode, request);
        return ResponseEntity.ok().build();
    }

    @GetMapping
    public List<ResidentWithPlatesResponse> listResidents() {
        return residentService.getAllResidentsWithPlates();
    }

    @DeleteMapping("/{residentId}")
    public ResponseEntity<Void> deleteResident(@PathVariable Long residentId) {
        residentService.deleteResident(residentId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/bulk-delete")
    public ResponseEntity<Void> bulkDelete(@RequestBody ResidentBulkDeleteRequest request) {
        residentService.deleteResidents(request.ids());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/export/excel")
    public ResponseEntity<byte[]> exportToExcel() throws IOException {
        byte[] excelContent = excelExportService.exportResidentsToExcel();

        String filename = "residents_" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) + ".xlsx";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
        headers.setContentDispositionFormData("attachment", filename);
        headers.setContentLength(excelContent.length);

        return ResponseEntity.ok()
                .headers(headers)
                .body(excelContent);
    }
}
