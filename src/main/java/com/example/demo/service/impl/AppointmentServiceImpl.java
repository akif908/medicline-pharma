package com.example.demo.service.impl;

import com.example.demo.model.*;
import com.example.demo.repository.*;
import com.example.demo.service.AppointmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AppointmentServiceImpl implements AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final DoctorSlotRepository doctorSlotRepository;
    private final UserRepository userRepository;
    private final ConsultationRepository consultationRepository;
    private final NotificationRepository notificationRepository;

    @Override
    @Transactional
    public Appointment createAppointment(Long patientId, Long doctorSlotId, String reason) {
        User patient = userRepository.findById(patientId).orElseThrow();
        DoctorSlot slot = doctorSlotRepository.findById(doctorSlotId).orElseThrow();
        
        if (slot.isBooked()) {
            throw new RuntimeException("This slot is already booked.");
        }
        
        slot.setBooked(true);
        doctorSlotRepository.save(slot);
        
        Appointment appt = new Appointment();
        appt.setPatient(patient);
        appt.setDoctor(slot.getDoctor());
        appt.setAppointmentDate(slot.getDate());
        appt.setStartTime(slot.getStartTime());
        appt.setEndTime(slot.getEndTime());
        appt.setReason(reason);
        appt.setStatus("PENDING");
        
        Appointment savedAppt = appointmentRepository.save(appt);
        
        // Notify Doctor
        Notification n = new Notification();
        n.setUser(slot.getDoctor().getUser());
        n.setMessage("New appointment request from " + patient.getName() + " for " + slot.getDate() + " at " + slot.getStartTime());
        notificationRepository.save(n);
        
        return savedAppt;
    }

    @Override
    public List<Appointment> getPatientAppointments(Long patientId) {
        return appointmentRepository.findByPatientIdOrderByAppointmentDateDescStartTimeDesc(patientId);
    }

    @Override
    public List<Appointment> getDoctorAppointments(Long doctorId) {
        return appointmentRepository.findByDoctorIdOrderByAppointmentDateDescStartTimeDesc(doctorId);
    }

    @Override
    @Transactional
    public Appointment approveAppointment(Long appointmentId) {
        Appointment appt = appointmentRepository.findById(appointmentId).orElseThrow();
        appt.setStatus("APPROVED");
        Appointment savedAppt = appointmentRepository.save(appt);
        
        // Create Consultation
        Consultation consultation = new Consultation();
        consultation.setPatient(appt.getPatient());
        consultation.setDoctor(appt.getDoctor());
        consultation.setStatus("OPEN");
        consultationRepository.save(consultation);
        
        // Notify Patient
        Notification n = new Notification();
        n.setUser(appt.getPatient());
        n.setMessage("Your appointment with Dr. " + appt.getDoctor().getUser().getName() + " on " + appt.getAppointmentDate() + " has been approved.");
        notificationRepository.save(n);
        
        return savedAppt;
    }

    @Override
    @Transactional
    public Appointment rejectAppointment(Long appointmentId) {
        Appointment appt = appointmentRepository.findById(appointmentId).orElseThrow();
        appt.setStatus("REJECTED");
        
        // Free the slot
        List<DoctorSlot> slots = doctorSlotRepository.findByDoctorIdAndDateOrderByStartTimeAsc(appt.getDoctor().getId(), appt.getAppointmentDate());
        for (DoctorSlot slot : slots) {
            if (slot.getStartTime().equals(appt.getStartTime()) && slot.getEndTime().equals(appt.getEndTime())) {
                slot.setBooked(false);
                doctorSlotRepository.save(slot);
                break;
            }
        }
        
        // Notify Patient
        Notification n = new Notification();
        n.setUser(appt.getPatient());
        n.setMessage("Your appointment request with Dr. " + appt.getDoctor().getUser().getName() + " was declined.");
        notificationRepository.save(n);
        
        return appointmentRepository.save(appt);
    }

    @Override
    @Transactional
    public Appointment completeAppointment(Long appointmentId) {
        Appointment appt = appointmentRepository.findById(appointmentId).orElseThrow();
        appt.setStatus("COMPLETED");
        
        // Close related consultations
        List<Consultation> consultations = consultationRepository.findByPatientId(appt.getPatient().getId());
        for (Consultation c : consultations) {
            if (c.getDoctor().getId().equals(appt.getDoctor().getId()) && "OPEN".equals(c.getStatus())) {
                c.setStatus("CLOSED");
                consultationRepository.save(c);
            }
        }
        
        return appointmentRepository.save(appt);
    }
}
