package com.analytics.service;

import com.analytics.domain.entity.DataFile;
import com.analytics.domain.entity.User;
import com.analytics.domain.repository.DataFileRepository;
import com.analytics.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;

/**
 * File Storage Service
 * Handles file upload, storage, and parsing
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class FileStorageService {

    private final DataFileRepository dataFileRepository;
    private final UserRepository userRepository;

    @Value("${app.file-storage.upload-dir}")
    private String uploadDir;

    /**
     * Upload and parse data file
     */
    @Transactional
    public DataFile uploadFile(MultipartFile file, String description) throws IOException {
        // Get current user
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Create upload directory if it doesn't exist
        Path uploadPath = Paths.get(uploadDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        // Generate unique filename
        String originalFilename = file.getOriginalFilename();
        String extension = originalFilename != null ?
                originalFilename.substring(originalFilename.lastIndexOf(".")) : "";
        String uniqueFilename = UUID.randomUUID().toString() + extension;
        Path filePath = uploadPath.resolve(uniqueFilename);

        // Save file
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        // Determine file type
        String fileType = determineFileType(originalFilename);

        // Parse file to get metadata
        Map<String, Object> metadata = parseFileMetadata(filePath, fileType);

        // Create DataFile entity
        DataFile dataFile = DataFile.builder()
                .fileName(originalFilename)
                .filePath(filePath.toString())
                .fileType(fileType)
                .fileSize(file.getSize())
                .rowCount((Integer) metadata.get("rowCount"))
                .columnCount((Integer) metadata.get("columnCount"))
                .description(description)
                .owner(user)
                .processed(false)
                .metadata(metadata.toString())
                .build();

        return dataFileRepository.save(dataFile);
    }

    /**
     * Parse file and extract data
     */
    public List<Map<String, Object>> parseFileData(Long fileId) throws IOException {
        DataFile dataFile = dataFileRepository.findById(fileId)
                .orElseThrow(() -> new RuntimeException("File not found"));

        Path filePath = Paths.get(dataFile.getFilePath());

        switch (dataFile.getFileType()) {
            case "EXCEL":
                return parseExcelFile(filePath);
            case "CSV":
                return parseCsvFile(filePath);
            default:
                throw new RuntimeException("Unsupported file type: " + dataFile.getFileType());
        }
    }

    /**
     * Get column names from file
     */
    public List<String> getColumnNames(Long fileId) throws IOException {
        DataFile dataFile = dataFileRepository.findById(fileId)
                .orElseThrow(() -> new RuntimeException("File not found"));

        Path filePath = Paths.get(dataFile.getFilePath());

        switch (dataFile.getFileType()) {
            case "EXCEL":
                return getExcelColumnNames(filePath);
            case "CSV":
                return getCsvColumnNames(filePath);
            default:
                throw new RuntimeException("Unsupported file type: " + dataFile.getFileType());
        }
    }

    /**
     * Delete file
     */
    @Transactional
    public void deleteFile(Long fileId) throws IOException {
        DataFile dataFile = dataFileRepository.findById(fileId)
                .orElseThrow(() -> new RuntimeException("File not found"));

        // Delete physical file
        Path filePath = Paths.get(dataFile.getFilePath());
        Files.deleteIfExists(filePath);

        // Delete database record
        dataFileRepository.delete(dataFile);
    }

    /**
     * Determine file type from filename
     */
    private String determineFileType(String filename) {
        if (filename == null) return "UNKNOWN";

        String lowerFilename = filename.toLowerCase();
        if (lowerFilename.endsWith(".xlsx") || lowerFilename.endsWith(".xls")) {
            return "EXCEL";
        } else if (lowerFilename.endsWith(".csv")) {
            return "CSV";
        } else if (lowerFilename.endsWith(".json")) {
            return "JSON";
        } else if (lowerFilename.endsWith(".sav")) {
            return "SPSS";
        }
        return "UNKNOWN";
    }

    /**
     * Parse file metadata (row count, column count)
     */
    private Map<String, Object> parseFileMetadata(Path filePath, String fileType) throws IOException {
        Map<String, Object> metadata = new HashMap<>();

        switch (fileType) {
            case "EXCEL":
                try (InputStream is = Files.newInputStream(filePath);
                     Workbook workbook = new XSSFWorkbook(is)) {
                    Sheet sheet = workbook.getSheetAt(0);
                    int rowCount = sheet.getLastRowNum();
                    int columnCount = 0;
                    if (rowCount > 0) {
                        Row headerRow = sheet.getRow(0);
                        columnCount = headerRow != null ? headerRow.getLastCellNum() : 0;
                    }
                    metadata.put("rowCount", rowCount);
                    metadata.put("columnCount", columnCount);
                }
                break;

            case "CSV":
                // Simple CSV row/column counting
                try (Scanner scanner = new Scanner(filePath)) {
                    int rowCount = 0;
                    int columnCount = 0;
                    while (scanner.hasNextLine()) {
                        String line = scanner.nextLine();
                        if (rowCount == 0) {
                            columnCount = line.split(",").length;
                        }
                        rowCount++;
                    }
                    metadata.put("rowCount", rowCount - 1); // Exclude header
                    metadata.put("columnCount", columnCount);
                }
                break;

            default:
                metadata.put("rowCount", 0);
                metadata.put("columnCount", 0);
        }

        return metadata;
    }

    /**
     * Parse Excel file data
     */
    private List<Map<String, Object>> parseExcelFile(Path filePath) throws IOException {
        List<Map<String, Object>> data = new ArrayList<>();

        try (InputStream is = Files.newInputStream(filePath);
             Workbook workbook = new XSSFWorkbook(is)) {

            Sheet sheet = workbook.getSheetAt(0);
            Row headerRow = sheet.getRow(0);

            // Get column names
            List<String> columnNames = new ArrayList<>();
            for (Cell cell : headerRow) {
                columnNames.add(getCellValueAsString(cell));
            }

            // Read data rows
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                Map<String, Object> rowData = new LinkedHashMap<>();
                for (int j = 0; j < columnNames.size(); j++) {
                    Cell cell = row.getCell(j);
                    String columnName = columnNames.get(j);
                    rowData.put(columnName, getCellValue(cell));
                }
                data.add(rowData);
            }
        }

        return data;
    }

    /**
     * Parse CSV file data
     */
    private List<Map<String, Object>> parseCsvFile(Path filePath) throws IOException {
        List<Map<String, Object>> data = new ArrayList<>();

        try (Scanner scanner = new Scanner(filePath)) {
            if (!scanner.hasNextLine()) return data;

            // Read header
            String headerLine = scanner.nextLine();
            String[] columnNames = headerLine.split(",");

            // Read data rows
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                String[] values = line.split(",");

                Map<String, Object> rowData = new LinkedHashMap<>();
                for (int i = 0; i < columnNames.length; i++) {
                    String columnName = columnNames[i].trim();
                    String value = i < values.length ? values[i].trim() : "";
                    rowData.put(columnName, value);
                }
                data.add(rowData);
            }
        }

        return data;
    }

    /**
     * Get Excel column names
     */
    private List<String> getExcelColumnNames(Path filePath) throws IOException {
        List<String> columnNames = new ArrayList<>();

        try (InputStream is = Files.newInputStream(filePath);
             Workbook workbook = new XSSFWorkbook(is)) {

            Sheet sheet = workbook.getSheetAt(0);
            Row headerRow = sheet.getRow(0);

            if (headerRow != null) {
                for (Cell cell : headerRow) {
                    columnNames.add(getCellValueAsString(cell));
                }
            }
        }

        return columnNames;
    }

    /**
     * Get CSV column names
     */
    private List<String> getCsvColumnNames(Path filePath) throws IOException {
        List<String> columnNames = new ArrayList<>();

        try (Scanner scanner = new Scanner(filePath)) {
            if (scanner.hasNextLine()) {
                String headerLine = scanner.nextLine();
                String[] columns = headerLine.split(",");
                for (String column : columns) {
                    columnNames.add(column.trim());
                }
            }
        }

        return columnNames;
    }

    /**
     * Get cell value as Object
     */
    private Object getCellValue(Cell cell) {
        if (cell == null) return null;

        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue();
                }
                return cell.getNumericCellValue();
            case BOOLEAN:
                return cell.getBooleanCellValue();
            case FORMULA:
                return cell.getCellFormula();
            default:
                return null;
        }
    }

    /**
     * Get cell value as String
     */
    private String getCellValueAsString(Cell cell) {
        if (cell == null) return "";

        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue().toString();
                }
                return String.valueOf(cell.getNumericCellValue());
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                return cell.getCellFormula();
            default:
                return "";
        }
    }
}
