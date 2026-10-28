package com.example.demo.controller;

import com.example.demo.model.MedicalHistory;
import com.example.demo.service.MedicalHistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/medical-history")
@RequiredArgsConstructor
public class MedicalHistoryController {

    private final MedicalHistoryService medicalHistoryService;

    @PostMapping
    public ResponseEntity<?> createMedicalHistory(@RequestBody Map<String, Object> payload) {
        try {
            Long consultationId = Long.valueOf(payload.get("consultationId").toString());
            String diagnosis = (String) payload.getOrDefault("diagnosis", "");
            String notes = (String) payload.getOrDefault("notes", "");
            MedicalHistory history = medicalHistoryService.createFromConsultation(consultationId, diagnosis, notes);
            return ResponseEntity.ok(history);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/patient/{patientId}")
    public List<MedicalHistory> getPatientHistory(@PathVariable Long patientId) {
        return medicalHistoryService.getPatientHistory(patientId);
    }

    @GetMapping("/patient/{patientId}/doctor/{doctorId}")
    public List<MedicalHistory> getPatientHistoryByDoctor(@PathVariable Long patientId, @PathVariable Long doctorId) {
        return medicalHistoryService.getPatientHistoryByDoctor(patientId, doctorId);
    }

    @GetMapping("/doctor/{doctorId}")
    public List<MedicalHistory> getDoctorHistory(@PathVariable Long doctorId) {
        return medicalHistoryService.getDoctorHistory(doctorId);
    }

    @GetMapping("/consultation/{consultationId}")
    public ResponseEntity<?> getByConsultation(@PathVariable Long consultationId) {
        MedicalHistory history = medicalHistoryService.getByConsultation(consultationId);
        if (history == null) {
            return ResponseEntity.ok(Map.of("exists", false));
        }
        return ResponseEntity.ok(history);
    }
}
