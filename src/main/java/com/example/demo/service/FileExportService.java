package com.example.demo.service;

import com.example.demo.model.MedicalHistory;
import com.example.demo.model.Order;
import com.example.demo.model.OrderItem;
import com.example.demo.repository.MedicalHistoryRepository;
import com.example.demo.repository.OrderRepository;
import com.example.demo.util.AuditLogger;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * FileExportService — provides file-based data export and receipt persistence.
 *
 * File I/O features:
 *  1. exportOrderReceiptToFile()   — Writes a human-readable .txt receipt for each order
 *                                    to receipts/{orderId}.txt using ObjectOutputStream
 *                                    wrapping a FileOutputStream (demonstrating Java serialization).
 *  2. exportMedicalHistoryToCsv()  — Writes a patient's medical history to CSV via BufferedWriter.
 *  3. exportOrdersToJson()         — Writes all orders to a JSON snapshot file using Files.write().
 *  4. cleanOldReceipts()           — Demonstrates file system traversal with Files.walk().
 *
 * All export paths are relative to the working directory under exports/.
 */
@Service
@RequiredArgsConstructor
public class FileExportService {

    private final OrderRepository orderRepository;
    private final MedicalHistoryRepository medicalHistoryRepository;
    private final AuditLogger auditLogger;

    private static final Path EXPORTS_DIR  = Paths.get("exports");
    private static final Path RECEIPTS_DIR = EXPORTS_DIR.resolve("receipts");
    private static final DateTimeFormatter FMT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    // ─── Initialization ────────────────────────────────────────────────────

    @jakarta.annotation.PostConstruct
    public void init() throws IOException {
        Files.createDirectories(RECEIPTS_DIR);
        Files.createDirectories(EXPORTS_DIR);
    }

    // ─── 1. Order Receipt (Java Serialization + FileOutputStream) ──────────

    /**
     * Writes a formatted order receipt to exports/receipts/{orderId}.txt.
     *
     * Uses FileOutputStream → BufferedOutputStream → PrintWriter (layered streams)
     * to demonstrate the decorator I/O pattern from the Java I/O class hierarchy.
     *
     * Also writes a binary serialized version to {orderId}.ser using ObjectOutputStream,
     * demonstrating Java object serialization for data persistence.
     */
    public void exportOrderReceiptToFile(Long orderId) {
        Order order = orderRepository.findById(orderId).orElse(null);
        if (order == null) return;

        // ── Text receipt ──────────────────────────────────────────────────
        Path txtPath = RECEIPTS_DIR.resolve(orderId + ".txt");
        try (PrintWriter pw = new PrintWriter(new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(txtPath.toFile()), StandardCharsets.UTF_8)))) {

            pw.println("╔══════════════════════════════════╗");
            pw.println("║   MEDICLINE PHARMA ORDER RECEIPT ║");
            pw.println("╚══════════════════════════════════╝");
            pw.printf("Order ID   : #%d%n", order.getId());
            pw.printf("Customer   : %s%n", order.getUser() != null ? order.getUser().getName() : "N/A");
            pw.printf("Status     : %s%n", order.getStatus());
            pw.printf("Address    : %s%n", order.getDeliveryAddress());
            pw.printf("Date       : %s%n", order.getTimestamp() != null ? order.getTimestamp().format(FMT) : "N/A");
            pw.println("──────────────────────────────────");
            pw.println("Items:");
            if (order.getItems() != null) {
                for (OrderItem item : order.getItems()) {
                    pw.printf("  %-28s x%d  $%.2f%n",
                            item.getMedicine() != null ? item.getMedicine().getName() : "Unknown",
                            item.getQuantity(), item.getPrice());
                }
            }
            pw.println("──────────────────────────────────");
            pw.printf("TOTAL      : $%.2f%n", order.getTotalPrice());
            pw.println("══════════════════════════════════");
            pw.printf("Generated  : %s%n", LocalDateTime.now().format(FMT));
            pw.printf("Thread     : %s%n", Thread.currentThread().getName());

        } catch (IOException e) {
            System.err.println("[FileExportService] Failed to write receipt: " + e.getMessage());
        }

        // ── Serialized binary snapshot ────────────────────────────────────
        // Stores a simple string-keyed data map as a serialized object file (.ser)
        Path serPath = RECEIPTS_DIR.resolve(orderId + ".ser");
        var receiptData = new java.util.LinkedHashMap<String, String>();
        receiptData.put("orderId",   String.valueOf(orderId));
        receiptData.put("customer",  order.getUser() != null ? order.getUser().getName() : "");
        receiptData.put("total",     String.format("%.2f", order.getTotalPrice()));
        receiptData.put("status",    order.getStatus());
        receiptData.put("generated", LocalDateTime.now().format(FMT));

        try (ObjectOutputStream oos = new ObjectOutputStream(
                new BufferedOutputStream(new FileOutputStream(serPath.toFile())))) {
            oos.writeObject(receiptData);
        } catch (IOException e) {
            System.err.println("[FileExportService] Serialization failed: " + e.getMessage());
        }

        auditLogger.log("ORDER_RECEIPT_EXPORTED",
                order.getUser() != null ? order.getUser().getName() : "unknown",
                "Receipt written to " + txtPath + " and " + serPath);
    }

    // ─── 2. Medical History CSV Export ────────────────────────────────────

    /**
     * Exports a patient's complete medical history to exports/medical_history_{userId}.csv.
     * Uses BufferedWriter for efficient character-by-character file writing.
     */
    public Path exportMedicalHistoryToCsv(Long userId) throws IOException {
        List<MedicalHistory> records = medicalHistoryRepository.findByPatientId(userId);
        Path csvPath = EXPORTS_DIR.resolve("medical_history_" + userId + ".csv");

        try (BufferedWriter bw = Files.newBufferedWriter(csvPath, StandardCharsets.UTF_8,
                StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {

            // CSV header
            bw.write("ID,Visit Date,Doctor,Specialization,Diagnosis,Notes");
            bw.newLine();

            for (MedicalHistory h : records) {
                String doctor = h.getDoctor() != null && h.getDoctor().getUser() != null
                        ? escape(h.getDoctor().getUser().getName()) : "";
                String spec   = h.getDoctor() != null ? escape(h.getDoctor().getSpecialization()) : "";
                String diag   = escape(h.getDiagnosis());
                String notes  = escape(h.getNotes());
                String date   = h.getVisitDate() != null ? h.getVisitDate().format(FMT) : "";

                bw.write(String.join(",",
                        String.valueOf(h.getId()), date, doctor, spec, diag, notes));
                bw.newLine();
            }
        }

        auditLogger.log("MEDICAL_HISTORY_EXPORTED", "user-" + userId,
                records.size() + " records written to " + csvPath);
        return csvPath;
    }

    // ─── 3. Full Order JSON Snapshot ─────────────────────────────────────

    /**
     * Writes a JSON snapshot of all orders to exports/orders_snapshot.json.
     * Demonstrates Files.write() with a byte array for simple JSON serialization
     * without requiring Jackson (manual JSON building).
     */
    public void exportOrdersToJsonSnapshot() throws IOException {
        List<Order> orders = orderRepository.findAll();
        Path jsonPath = EXPORTS_DIR.resolve("orders_snapshot.json");

        StringBuilder sb = new StringBuilder("[\n");
        for (int i = 0; i < orders.size(); i++) {
            Order o = orders.get(i);
            sb.append("  {\n");
            sb.append("    \"id\": ").append(o.getId()).append(",\n");
            sb.append("    \"customer\": \"")
                    .append(o.getUser() != null ? escape(o.getUser().getName()) : "").append("\",\n");
            sb.append("    \"status\": \"").append(o.getStatus()).append("\",\n");
            sb.append("    \"totalPrice\": ").append(String.format("%.2f", o.getTotalPrice())).append(",\n");
            sb.append("    \"date\": \"")
                    .append(o.getTimestamp() != null ? o.getTimestamp().format(FMT) : "").append("\"\n");
            sb.append("  }").append(i < orders.size() - 1 ? "," : "").append("\n");
        }
        sb.append("]");

        // Files.write() — atomic single-call file write
        Files.write(jsonPath, sb.toString().getBytes(StandardCharsets.UTF_8),
                StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);

        auditLogger.log("ORDERS_SNAPSHOT_EXPORTED", "SYSTEM",
                orders.size() + " orders exported to " + jsonPath);
    }

    // ─── 4. Cleanup old receipts ─────────────────────────────────────────

    /**
     * Deletes receipt files older than 90 days.
     * Demonstrates Files.walk() for recursive directory traversal.
     */
    public void cleanOldReceipts() throws IOException {
        long cutoffMs = System.currentTimeMillis() - (90L * 24 * 60 * 60 * 1000);
        try (var stream = Files.walk(RECEIPTS_DIR, 1)) {
            stream.filter(Files::isRegularFile)
                  .filter(p -> p.toFile().lastModified() < cutoffMs)
                  .forEach(p -> {
                      try {
                          Files.delete(p);
                          System.out.println("[FileExportService] Deleted old receipt: " + p);
                      } catch (IOException e) {
                          System.err.println("[FileExportService] Could not delete: " + p);
                      }
                  });
        }
    }

    // ─── CSV helper ───────────────────────────────────────────────────────

    /** Wraps a CSV field in quotes and escapes internal quotes. */
    private String escape(String value) {
        if (value == null) return "\"\"";
        return "\"" + value.replace("\"", "\"\"") + "\"";
    }
}
