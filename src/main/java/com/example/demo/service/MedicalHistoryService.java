package com.example.demo.service;

import com.example.demo.model.MedicalHistory;
import java.util.List;

public interface MedicalHistoryService {
    MedicalHistory createFromConsultation(Long consultationId, String diagnosis, String notes);
    List<MedicalHistory> getPatientHistory(Long patientId);
    List<MedicalHistory> getPatientHistoryByDoctor(Long patientId, Long doctorId);
    List<MedicalHistory> getDoctorHistory(Long doctorId);
    MedicalHistory getByConsultation(Long consultationId);
}
