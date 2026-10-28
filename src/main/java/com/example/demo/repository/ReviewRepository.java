package com.example.demo.repository;

import com.example.demo.model.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;
import java.util.Optional;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    List<Review> findByDoctorId(Long doctorId);
    List<Review> findByPatientId(Long patientId);
    Optional<Review> findByConsultationId(Long consultationId);

    @Query("SELECT ROUND(AVG(r.rating), 1) FROM Review r WHERE r.doctor.id = ?1")
    Double findAverageRatingByDoctorId(Long doctorId);
}
