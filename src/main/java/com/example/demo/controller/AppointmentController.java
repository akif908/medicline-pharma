package com.example.demo.controller;

import com.example.demo.model.Appointment;
import com.example.demo.model.Doctor;
import com.example.demo.repository.DoctorRepository;
import com.example.demo.service.AppointmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/appointments")
@RequiredArgsConstructor
public class AppointmentController {

    private final AppointmentService appointmentService;
    private final DoctorRepository doctorRepository;

    @PostMapping
    public Appointment createAppointment(@RequestBody Map<String, Object> payload) {
        Long patientId = Long.valueOf(payload.get("patientId").toString());
        Long doctorSlotId = Long.valueOf(payload.get("doctorSlotId").toString());
        String reason = payload.get("reason").toString();
        return appointmentService.createAppointment(patientId, doctorSlotId, reason);
    }

    @GetMapping("/patient/{patientId}")
    public List<Appointment> getPatientAppointments(@PathVariable Long patientId) {
        return appointmentService.getPatientAppointments(patientId);
    }

    @GetMapping("/doctor/{doctorId}")
    public List<Appointment> getDoctorAppointments(@PathVariable Long doctorId) {
        return appointmentService.getDoctorAppointments(doctorId);
    }

    /** Fetch appointments using the doctor's User ID (not Doctor entity ID) */
    @GetMapping("/by-user/{userId}")
    public List<Appointment> getAppointmentsByUserId(@PathVariable Long userId) {
        Doctor doctor = doctorRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("No doctor found for user " + userId));
        return appointmentService.getDoctorAppointments(doctor.getId());
    }

    @PostMapping("/{id}/approve")
    public Appointment approveAppointment(@PathVariable Long id) {
        return appointmentService.approveAppointment(id);
    }

    @PostMapping("/{id}/reject")
    public Appointment rejectAppointment(@PathVariable Long id) {
        return appointmentService.rejectAppointment(id);
    }

    @PostMapping("/{id}/complete")
    public Appointment completeAppointment(@PathVariable Long id) {
        return appointmentService.completeAppointment(id);
    }
}
