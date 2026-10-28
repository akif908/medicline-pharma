package com.example.demo.service.impl;

import com.example.demo.model.*;
import com.example.demo.repository.*;
import com.example.demo.service.MedicalHistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MedicalHistoryServiceImpl implements MedicalHistoryService {

    private final MedicalHistoryRepository medicalHistoryRepository;
    private final ConsultationRepository consultationRepository;
    private final UserRepository userRepository;
    private final DoctorRepository doctorRepository;
    private final PrescriptionRepository prescriptionRepository;

    @Override
    public MedicalHistory createFromConsultation(Long consultationId, String diagnosis, String notes) {
        Consultation consultation = consultationRepository.findById(consultationId)
                .orElseThrow(() -> new RuntimeException("Consultation not found"));

        MedicalHistory history = new MedicalHistory();
        history.setPatient(consultation.getPatient());
        history.setDoctor(consultation.getDoctor());
        history.setConsultation(consultation);

        Prescription prescription = prescriptionRepository.findByConsultationId(consultationId).orElse(null);
        history.setPrescription(prescription != null ? prescription.getContent() : "");

        history.setDiagnosis(diagnosis != null ? diagnosis : "");
        history.setNotes(notes != null ? notes : "");
        return medicalHistoryRepository.save(history);
    }

    @Override
    public List<MedicalHistory> getPatientHistory(Long patientId) {
        User patient = userRepository.findById(patientId)
                .orElseThrow(() -> new RuntimeException("Patient not found"));
        return medicalHistoryRepository.findByPatientOrderByVisitDateDesc(patient);
    }

    @Override
    public List<MedicalHistory> getPatientHistoryByDoctor(Long patientId, Long doctorId) {
        User patient = userRepository.findById(patientId)
                .orElseThrow(() -> new RuntimeException("Patient not found"));
        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new RuntimeException("Doctor not found"));
        return medicalHistoryRepository.findByPatientAndDoctorOrderByVisitDateDesc(patient, doctor);
    }

    @Override
    public List<MedicalHistory> getDoctorHistory(Long doctorId) {
        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new RuntimeException("Doctor not found"));
        return medicalHistoryRepository.findByDoctorOrderByVisitDateDesc(doctor);
    }

    @Override
    public MedicalHistory getByConsultation(Long consultationId) {
        return medicalHistoryRepository.findAll().stream()
                .filter(h -> h.getConsultation() != null && h.getConsultation().getId().equals(consultationId))
                .findFirst().orElse(null);
    }
}
