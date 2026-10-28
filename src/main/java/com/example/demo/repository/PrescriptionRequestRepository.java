package com.example.demo.repository;

import com.example.demo.model.PrescriptionRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PrescriptionRequestRepository extends JpaRepository<PrescriptionRequest, Long> {
    List<PrescriptionRequest> findByUserIdOrderByCreatedAtDesc(Long userId);
    List<PrescriptionRequest> findByStatusOrderByCreatedAtDesc(String status);
    long countByStatus(String status);
}
