package com.example.demo.controller;

import com.example.demo.service.FileExportService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * FileExportController — REST endpoints that trigger file-based exports.
 *
 * These endpoints allow the admin or patient to download files generated
 * by FileExportService (CSV, JSON snapshots, audit log).
 *
 * Uses FileSystemResource (the non-deprecated replacement for PathResource
 * since Spring 7.0) to serve files from the local filesystem.
 */
@RestController
@RequestMapping("/api/export")
@RequiredArgsConstructor
public class FileExportController {

    private final FileExportService fileExportService;

    /**
     * GET /api/export/medical-history/{userId}
     * Downloads the patient's medical history as a CSV file.
     */
    @GetMapping("/medical-history/{userId}")
    public ResponseEntity<Resource> exportMedicalHistory(@PathVariable Long userId) {
        try {
            Path csvPath = fileExportService.exportMedicalHistoryToCsv(userId);
            Resource resource = new FileSystemResource(csvPath);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"medical_history_" + userId + ".csv\"")
                    .contentType(MediaType.parseMediaType("text/csv"))
                    .body(resource);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * GET /api/export/orders-snapshot
     * Generates and downloads a JSON snapshot of all orders (admin only).
     */
    @GetMapping("/orders-snapshot")
    public ResponseEntity<Resource> exportOrdersSnapshot() {
        try {
            fileExportService.exportOrdersToJsonSnapshot();
            Resource resource = new FileSystemResource(
                    Paths.get("exports/orders_snapshot.json"));
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"orders_snapshot.json\"")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(resource);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * GET /api/export/audit-log
     * Downloads the audit.log file (admin only).
     */
    @GetMapping("/audit-log")
    public ResponseEntity<Resource> downloadAuditLog() {
        try {
            Resource resource = new FileSystemResource(
                    Paths.get("logs/audit.log"));
            if (!resource.exists()) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"audit.log\"")
                    .contentType(MediaType.TEXT_PLAIN)
                    .body(resource);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
