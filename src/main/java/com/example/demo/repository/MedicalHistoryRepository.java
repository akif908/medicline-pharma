package com.example.demo.repository;

import com.example.demo.model.Doctor;
import com.example.demo.model.MedicalHistory;
import com.example.demo.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MedicalHistoryRepository extends JpaRepository<MedicalHistory, Long> {
    List<MedicalHistory> findByPatientOrderByVisitDateDesc(User patient);
    List<MedicalHistory> findByPatientAndDoctorOrderByVisitDateDesc(User patient, Doctor doctor);
    List<MedicalHistory> findByDoctorOrderByVisitDateDesc(Doctor doctor);
    List<MedicalHistory> findByPatientId(Long patientId);
}
