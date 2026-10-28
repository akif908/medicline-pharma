package com.example.demo.repository;

import com.example.demo.model.DoctorSlot;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.List;

public interface DoctorSlotRepository extends JpaRepository<DoctorSlot, Long> {
    List<DoctorSlot> findByDoctorIdAndDateOrderByStartTimeAsc(Long doctorId, LocalDate date);
    List<DoctorSlot> findByDoctorIdAndDateGreaterThanEqualOrderByDateAscStartTimeAsc(Long doctorId, LocalDate date);
}
