package com.example.demo.service;

import com.example.demo.model.Consultation;
import com.example.demo.model.Message;
import com.example.demo.model.Prescription;

import java.util.List;

public interface ConsultationService {
    Consultation createConsultation(Long patientId, Long doctorId);
    List<Consultation> getPatientConsultations(Long patientId);
    List<Consultation> getDoctorConsultations(Long doctorId);
    Consultation getConsultationById(Long id);
    List<Consultation> getAllConsultations();
    
    Message sendMessage(Long consultationId, Long senderId, String content, String imageUrl);
    List<Message> getMessages(Long consultationId);
    
    Prescription createPrescription(Long consultationId, Long doctorId, String content);
    Prescription getPrescriptionByConsultation(Long consultationId);

    Consultation closeConsultation(Long consultationId);
}
