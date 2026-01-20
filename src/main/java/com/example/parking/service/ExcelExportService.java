package com.example.parking.service;

import com.example.parking.dto.ResidentWithPlatesResponse;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class ExcelExportService {

    private final ResidentService residentService;

    public ExcelExportService(ResidentService residentService) {
        this.residentService = residentService;
    }

    public byte[] exportResidentsToExcel() throws IOException {
        List<ResidentWithPlatesResponse> residents = residentService.getAllResidentsWithPlates();

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Residents");

            // Create header style
            CellStyle headerStyle = workbook.createCellStyle();
            headerStyle.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            Font headerFont = workbook.createFont();
            headerFont.setColor(IndexedColors.WHITE.getIndex());
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);

            headerStyle.setBorderBottom(BorderStyle.THIN);
            headerStyle.setBorderTop(BorderStyle.THIN);
            headerStyle.setBorderLeft(BorderStyle.THIN);
            headerStyle.setBorderRight(BorderStyle.THIN);

            // Create data cell style
            CellStyle dataStyle = workbook.createCellStyle();
            dataStyle.setBorderBottom(BorderStyle.THIN);
            dataStyle.setBorderTop(BorderStyle.THIN);
            dataStyle.setBorderLeft(BorderStyle.THIN);
            dataStyle.setBorderRight(BorderStyle.THIN);

            // Alternate row style
            CellStyle alternateStyle = workbook.createCellStyle();
            alternateStyle.cloneStyleFrom(dataStyle);
            alternateStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            alternateStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            // Create header row
            Row headerRow = sheet.createRow(0);
            String[] headers = {"ID", "Unique Code", "Name", "Address", "License Plates", "Plate Count"};

            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // Create data rows
            int rowNum = 1;
            for (ResidentWithPlatesResponse resident : residents) {
                Row row = sheet.createRow(rowNum);
                CellStyle rowStyle = (rowNum % 2 == 0) ? alternateStyle : dataStyle;

                Cell idCell = row.createCell(0);
                idCell.setCellValue(resident.id());
                idCell.setCellStyle(rowStyle);

                Cell codeCell = row.createCell(1);
                codeCell.setCellValue(resident.uniqueCode());
                codeCell.setCellStyle(rowStyle);

                Cell nameCell = row.createCell(2);
                nameCell.setCellValue(resident.name() != null ? resident.name() : "");
                nameCell.setCellStyle(rowStyle);

                Cell addressCell = row.createCell(3);
                addressCell.setCellValue(resident.address() != null ? resident.address() : "");
                addressCell.setCellStyle(rowStyle);

                Cell platesCell = row.createCell(4);
                String platesString = resident.licensePlates() != null ? String.join(", ", resident.licensePlates()) : "";
                platesCell.setCellValue(platesString);
                platesCell.setCellStyle(rowStyle);

                Cell countCell = row.createCell(5);
                countCell.setCellValue(resident.licensePlates() != null ? resident.licensePlates().size() : 0);
                countCell.setCellStyle(rowStyle);

                rowNum++;
            }

            // Auto-size columns
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
                // Add a little extra width
                int currentWidth = sheet.getColumnWidth(i);
                sheet.setColumnWidth(i, currentWidth + 512);
            }

            // Add summary sheet
            Sheet summarySheet = workbook.createSheet("Summary");

            Row summaryHeader = summarySheet.createRow(0);
            Cell summaryHeaderCell = summaryHeader.createCell(0);
            summaryHeaderCell.setCellValue("Residents Export Summary");
            summaryHeaderCell.setCellStyle(headerStyle);

            Cell summaryHeaderCell2 = summaryHeader.createCell(1);
            summaryHeaderCell2.setCellStyle(headerStyle);

            Row totalRow = summarySheet.createRow(2);
            totalRow.createCell(0).setCellValue("Total Residents:");
            totalRow.createCell(1).setCellValue(residents.size());

            int totalPlates = residents.stream()
                    .mapToInt(r -> r.licensePlates() != null ? r.licensePlates().size() : 0)
                    .sum();

            Row platesRow = summarySheet.createRow(3);
            platesRow.createCell(0).setCellValue("Total License Plates:");
            platesRow.createCell(1).setCellValue(totalPlates);

            Row dateRow = summarySheet.createRow(5);
            dateRow.createCell(0).setCellValue("Export Date:");
            dateRow.createCell(1).setCellValue(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));

            summarySheet.autoSizeColumn(0);
            summarySheet.autoSizeColumn(1);

            // Write to byte array
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            return outputStream.toByteArray();
        }
    }
}

