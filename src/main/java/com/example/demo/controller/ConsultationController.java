package com.example.demo.controller;

import com.example.demo.model.Consultation;
import com.example.demo.model.Message;
import com.example.demo.service.ConsultationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/consultations")
@RequiredArgsConstructor
public class ConsultationController {

    private final ConsultationService consultationService;

    @PostMapping
    public Consultation createConsultation(@RequestBody Map<String, Long> payload) {
        return consultationService.createConsultation(payload.get("patientId"), payload.get("doctorId"));
    }

    @GetMapping
    public List<Consultation> getAllConsultations() {
        return consultationService.getAllConsultations();
    }

    @GetMapping("/patient/{patientId}")
    public List<Consultation> getPatientConsultations(@PathVariable Long patientId) {
        return consultationService.getPatientConsultations(patientId);
    }

    @GetMapping("/doctor/{doctorId}")
    public List<Consultation> getDoctorConsultations(@PathVariable Long doctorId) {
        return consultationService.getDoctorConsultations(doctorId);
    }

    @GetMapping("/{id}/messages")
    public List<Message> getMessages(@PathVariable Long id) {
        return consultationService.getMessages(id);
    }

    @PostMapping("/{id}/messages")
    public Message sendMessage(@PathVariable Long id, @RequestBody Map<String, Object> payload) {
        Long senderId = Long.parseLong(payload.get("senderId").toString());
        String content = (String) payload.getOrDefault("content", "");
        String imageUrl = (String) payload.getOrDefault("imageUrl", null);
        return consultationService.sendMessage(id, senderId, content, imageUrl);
    }

    @PostMapping("/{id}/close")
    public Consultation closeConsultation(@PathVariable Long id) {
        return consultationService.closeConsultation(id);
    }
}
