package com.example.demo.util;

import org.springframework.stereotype.Component;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * AuditLogger — writes significant system events to a persistent log file.
 *
 * File I/O pattern:
 *  - Uses java.nio.file.Files.write / BufferedWriter for efficient file-based persistence.
 *  - The log file (audit.log) is stored under logs/ relative to the working directory.
 *  - APPEND mode ensures entries accumulate across restarts rather than being overwritten.
 *  - The write method is synchronized to prevent garbled log lines from concurrent threads.
 *
 * Events logged:
 *  ORDER_PLACED, ORDER_APPROVED, PRESCRIPTION_APPROVED, PRESCRIPTION_REJECTED,
 *  APPOINTMENT_APPROVED, USER_REGISTERED, MEDICINE_STOCK_CHANGED
 */
@Component
public class AuditLogger {

    private static final Path LOG_DIR  = Paths.get("logs");
    private static final Path LOG_FILE = LOG_DIR.resolve("audit.log");
    private static final DateTimeFormatter FMT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public AuditLogger() {
        // Ensure logs directory exists at startup
        try {
            if (!Files.exists(LOG_DIR)) {
                Files.createDirectories(LOG_DIR);
            }
            if (!Files.exists(LOG_FILE)) {
                Files.createFile(LOG_FILE);
                appendLine("=== Medicline Pharma Audit Log Initialized ===");
            }
        } catch (IOException e) {
            System.err.println("[AuditLogger] Failed to initialize log file: " + e.getMessage());
        }
    }

    /**
     * Writes a structured audit entry to audit.log.
     * Format: [2026-07-13 18:00:00] [EVENT_TYPE] [actor] detail
     *
     * Thread-safe: synchronized prevents concurrent writes from interleaving.
     */
    public synchronized void log(String eventType, String actor, String detail) {
        String timestamp = LocalDateTime.now().format(FMT);
        String thread    = Thread.currentThread().getName();
        String entry     = String.format("[%s] [%-30s] [%-20s] [thread: %-20s] %s%n",
                timestamp, eventType, actor, thread, detail);
        appendLine(entry);
    }

    /** Convenience overload for system events without a user actor. */
    public synchronized void log(String eventType, String detail) {
        log(eventType, "SYSTEM", detail);
    }

    private void appendLine(String text) {
        try (BufferedWriter writer = Files.newBufferedWriter(
                LOG_FILE,
                StandardCharsets.UTF_8,
                StandardOpenOption.APPEND,    // never truncate the file
                StandardOpenOption.CREATE     // create if missing
        )) {
            writer.write(text);
            writer.flush();
        } catch (IOException e) {
            System.err.printf("[AuditLogger] Write failed: %s%n", e.getMessage());
        }
    }
}
