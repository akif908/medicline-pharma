package com.example.demo.service.impl;

import com.example.demo.model.*;
import com.example.demo.repository.*;
import com.example.demo.service.ConsultationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ConsultationServiceImpl implements ConsultationService {

    private final ConsultationRepository consultationRepository;
    private final MessageRepository messageRepository;
    private final PrescriptionRepository prescriptionRepository;
    private final UserRepository userRepository;
    private final DoctorRepository doctorRepository;
    private final com.example.demo.repository.NotificationRepository notificationRepository;
    private final com.example.demo.repository.AppointmentRepository appointmentRepository;

    @Override
    public Consultation createConsultation(Long patientId, Long doctorId) {
        User patient = userRepository.findById(patientId).orElseThrow();
        Doctor doctor = doctorRepository.findById(doctorId).orElseThrow();
        
        Consultation consultation = new Consultation();
        consultation.setPatient(patient);
        consultation.setDoctor(doctor);
        consultation.setStatus("OPEN");
        return consultationRepository.save(consultation);
    }

    @Override
    public List<Consultation> getPatientConsultations(Long patientId) {
        return consultationRepository.findByPatientId(patientId);
    }

    @Override
    public List<Consultation> getDoctorConsultations(Long doctorId) {
        return consultationRepository.findByDoctorId(doctorId);
    }

    @Override
    public Consultation getConsultationById(Long id) {
        return consultationRepository.findById(id).orElse(null);
    }

    @Override
    public List<Consultation> getAllConsultations() {
        return consultationRepository.findAll();
    }

    @Override
    public Message sendMessage(Long consultationId, Long senderId, String content, String imageUrl) {
        Consultation consultation = consultationRepository.findById(consultationId).orElseThrow();
        User sender = userRepository.findById(senderId).orElseThrow();
        
        Message msg = new Message();
        msg.setConsultation(consultation);
        msg.setSender(sender);
        msg.setContent(content != null ? content : "");
        msg.setImageUrl(imageUrl);
        Message savedMsg = messageRepository.save(msg);

        // If doctor replied, notify patient
        if ("DOCTOR".equals(sender.getRole().name())) {
            com.example.demo.model.Notification n = new com.example.demo.model.Notification();
            n.setUser(consultation.getPatient());
            n.setMessage("Doctor " + sender.getName() + " replied to your consultation.");
            notificationRepository.save(n);
        } else if ("PATIENT".equals(sender.getRole().name())) {
            // Optional: Notify doctor
            com.example.demo.model.Notification n = new com.example.demo.model.Notification();
            n.setUser(consultation.getDoctor().getUser());
            n.setMessage("Patient " + sender.getName() + " sent a message.");
            notificationRepository.save(n);
        }

        return savedMsg;
    }

    @Override
    public List<Message> getMessages(Long consultationId) {
        return messageRepository.findByConsultationIdOrderByTimestampAsc(consultationId);
    }

    @Override
    public Prescription createPrescription(Long consultationId, Long doctorId, String content) {
        Consultation consultation = consultationRepository.findById(consultationId).orElseThrow();
        Doctor doctor = doctorRepository.findById(doctorId).orElseThrow();
        
        Prescription prescription = new Prescription();
        prescription.setConsultation(consultation);
        prescription.setDoctor(doctor);
        prescription.setContent(content);
        return prescriptionRepository.save(prescription);
    }

    @Override
    public Prescription getPrescriptionByConsultation(Long consultationId) {
        // Typically a query method in repository: findByConsultationId
        return null; // Will implement query later if needed
    }

    @Override
    @Transactional
    public Consultation closeConsultation(Long consultationId) {
        Consultation consultation = consultationRepository.findById(consultationId).orElseThrow();
        consultation.setStatus("CLOSED");
        Consultation saved = consultationRepository.save(consultation);

        // Complete the related approved appointment
        List<Appointment> appts = appointmentRepository.findByDoctorIdOrderByAppointmentDateDescStartTimeDesc(
                consultation.getDoctor().getId());
        for (Appointment a : appts) {
            if (a.getPatient().getId().equals(consultation.getPatient().getId()) && "APPROVED".equals(a.getStatus())) {
                a.setStatus("COMPLETED");
                appointmentRepository.save(a);
                break;
            }
        }

        // Notify patient
        Notification n = new Notification();
        n.setUser(consultation.getPatient());
        n.setMessage("Your consultation with Dr. " + consultation.getDoctor().getUser().getName() + " has been closed by the doctor.");
        notificationRepository.save(n);

        return saved;
    }
}
