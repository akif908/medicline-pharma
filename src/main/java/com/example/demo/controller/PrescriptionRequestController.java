package com.example.demo.controller;

import com.example.demo.model.*;
import com.example.demo.repository.*;
import com.example.demo.util.AuditLogger;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/prescription-requests")
@RequiredArgsConstructor
public class PrescriptionRequestController {

    private final PrescriptionRequestRepository prescriptionRequestRepository;
    private final UserRepository userRepository;
    private final MedicineRepository medicineRepository;
    private final NotificationRepository notificationRepository;
    private final AuditLogger auditLogger;

    // ─── USER: Submit a prescription request ───────────────────────────────────
    @PostMapping
    public ResponseEntity<?> submitRequest(@RequestBody Map<String, Object> body) {
        try {
            Long userId = Long.parseLong(body.get("userId").toString());
            Long medicineId = Long.parseLong(body.get("medicineId").toString());
            String patientNote = body.getOrDefault("patientNote", "").toString();
            String imageUrl = body.getOrDefault("prescriptionImageUrl", "").toString();

            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            Medicine medicine = medicineRepository.findById(medicineId)
                    .orElseThrow(() -> new RuntimeException("Medicine not found"));

            // Check if there's already a PENDING or APPROVED request for this medicine
            boolean alreadyExists = prescriptionRequestRepository
                    .findByUserIdOrderByCreatedAtDesc(userId).stream()
                    .anyMatch(r -> r.getMedicine().getId().equals(medicineId)
                            && ("PENDING".equals(r.getStatus()) || "APPROVED".equals(r.getStatus())));
            if (alreadyExists) {
                return ResponseEntity.badRequest()
                        .body("You already have an active or approved request for this medicine.");
            }

            PrescriptionRequest req = new PrescriptionRequest();
            req.setUser(user);
            req.setMedicine(medicine);
            req.setPatientNote(patientNote);
            req.setPrescriptionImageUrl(imageUrl.isEmpty() ? null : imageUrl);
            prescriptionRequestRepository.save(req);

            // Notify all ADMINs
            userRepository.findAll().stream()
                    .filter(u -> u.getRole() == Role.ADMIN)
                    .forEach(admin -> {
                        Notification n = new Notification();
                        n.setUser(admin);
                        n.setMessage("📋 New prescription request from " + user.getName()
                                + " for " + medicine.getName() + ". Please review.");
                        notificationRepository.save(n);
                    });

            return ResponseEntity.ok(req);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // ─── USER: Get their own requests ──────────────────────────────────────────
    @GetMapping("/user/{userId}")
    public List<PrescriptionRequest> getUserRequests(@PathVariable Long userId) {
        return prescriptionRequestRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    // ─── USER: Check if approved for a specific medicine ───────────────────────
    @GetMapping("/user/{userId}/medicine/{medicineId}/approved")
    public ResponseEntity<Boolean> isApproved(@PathVariable Long userId,
                                               @PathVariable Long medicineId) {
        boolean approved = prescriptionRequestRepository
                .findByUserIdOrderByCreatedAtDesc(userId).stream()
                .anyMatch(r -> r.getMedicine().getId().equals(medicineId)
                        && "APPROVED".equals(r.getStatus()));
        return ResponseEntity.ok(approved);
    }

    // ─── ADMIN: Get all requests (optionally filtered by status) ───────────────
    @GetMapping
    public List<PrescriptionRequest> getAllRequests(
            @RequestParam(required = false) String status) {
        if (status != null && !status.isBlank()) {
            return prescriptionRequestRepository.findByStatusOrderByCreatedAtDesc(status);
        }
        return prescriptionRequestRepository.findAll()
                .stream()
                .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
                .toList();
    }

    // ─── ADMIN: Pending count (for notification badge) ─────────────────────────
    @GetMapping("/pending/count")
    public ResponseEntity<Long> pendingCount() {
        return ResponseEntity.ok(prescriptionRequestRepository.countByStatus("PENDING"));
    }

    // ─── ADMIN: Approve ────────────────────────────────────────────────────────
    @PutMapping("/{id}/approve")
    public ResponseEntity<?> approve(@PathVariable Long id,
                                     @RequestBody(required = false) Map<String, String> body) {
        return prescriptionRequestRepository.findById(id).map(req -> {
            req.setStatus("APPROVED");
            if (body != null && body.containsKey("adminNote")) {
                req.setAdminNote(body.get("adminNote"));
            }
            prescriptionRequestRepository.save(req);

            // Notify user
            Notification n = new Notification();
            n.setUser(req.getUser());
            n.setMessage("✅ Your prescription request for " + req.getMedicine().getName()
                    + " has been APPROVED! You can now purchase it.");
            notificationRepository.save(n);

            // Audit log
            auditLogger.log("PRESCRIPTION_APPROVED",
                    req.getUser().getName(),
                    "Medicine: " + req.getMedicine().getName() + " | Request #" + req.getId());

            return ResponseEntity.ok(req);
        }).orElse(ResponseEntity.notFound().build());
    }

    // ─── ADMIN: Reject ─────────────────────────────────────────────────────────
    @PutMapping("/{id}/reject")
    public ResponseEntity<?> reject(@PathVariable Long id,
                                    @RequestBody(required = false) Map<String, String> body) {
        return prescriptionRequestRepository.findById(id).map(req -> {
            req.setStatus("REJECTED");
            if (body != null && body.containsKey("adminNote")) {
                req.setAdminNote(body.get("adminNote"));
            }
            prescriptionRequestRepository.save(req);

            // Notify user
            Notification n = new Notification();
            n.setUser(req.getUser());
            n.setMessage("❌ Your prescription request for " + req.getMedicine().getName()
                    + " was REJECTED. Reason: "
                    + (body != null ? body.getOrDefault("adminNote", "No reason given") : "No reason given"));
            notificationRepository.save(n);

            // Audit log
            auditLogger.log("PRESCRIPTION_REJECTED",
                    req.getUser().getName(),
                    "Medicine: " + req.getMedicine().getName() + " | Request #" + req.getId()
                    + " | Reason: " + (body != null ? body.getOrDefault("adminNote", "") : ""));

            return ResponseEntity.ok(req);
        }).orElse(ResponseEntity.notFound().build());
    }
}
