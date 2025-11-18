package com.analytics.api.controller;

import com.analytics.api.dto.MessageResponse;
import com.analytics.domain.entity.DataFile;
import com.analytics.service.FileStorageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * File Upload REST Controller
 * Handles data file upload and management
 */
@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
@Tag(name = "File Management", description = "File upload and management APIs")
public class FileUploadController {

    private final FileStorageService fileStorageService;

    @PostMapping("/upload")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Upload data file", description = "Upload Excel or CSV file for analysis")
    public ResponseEntity<?> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "description", required = false) String description) {
        try {
            // Validate file
            if (file.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(new MessageResponse("Please select a file to upload"));
            }

            // Validate file type
            String filename = file.getOriginalFilename();
            if (filename == null || (!filename.endsWith(".xlsx") &&
                    !filename.endsWith(".xls") && !filename.endsWith(".csv"))) {
                return ResponseEntity.badRequest()
                        .body(new MessageResponse("Only Excel (.xlsx, .xls) and CSV (.csv) files are supported"));
            }

            DataFile dataFile = fileStorageService.uploadFile(file, description);

            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                    "message", "File uploaded successfully",
                    "fileId", dataFile.getId(),
                    "fileName", dataFile.getFileName(),
                    "rowCount", dataFile.getRowCount(),
                    "columnCount", dataFile.getColumnCount()
            ));
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MessageResponse("Failed to upload file: " + e.getMessage()));
        }
    }

    @GetMapping("/{fileId}/data")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get file data", description = "Retrieve parsed data from uploaded file")
    public ResponseEntity<?> getFileData(@PathVariable Long fileId) {
        try {
            List<Map<String, Object>> data = fileStorageService.parseFileData(fileId);
            return ResponseEntity.ok(data);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MessageResponse("Failed to read file: " + e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new MessageResponse(e.getMessage()));
        }
    }

    @GetMapping("/{fileId}/columns")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get file columns", description = "Retrieve column names from uploaded file")
    public ResponseEntity<?> getFileColumns(@PathVariable Long fileId) {
        try {
            List<String> columns = fileStorageService.getColumnNames(fileId);
            return ResponseEntity.ok(Map.of("columns", columns));
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MessageResponse("Failed to read file: " + e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new MessageResponse(e.getMessage()));
        }
    }

    @DeleteMapping("/{fileId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Delete file", description = "Delete uploaded file")
    public ResponseEntity<?> deleteFile(@PathVariable Long fileId) {
        try {
            fileStorageService.deleteFile(fileId);
            return ResponseEntity.ok(new MessageResponse("File deleted successfully"));
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MessageResponse("Failed to delete file: " + e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new MessageResponse(e.getMessage()));
        }
    }
}
