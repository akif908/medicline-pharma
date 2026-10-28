package com.example.demo.service;

import com.example.demo.model.DoctorSlot;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public interface DoctorSlotService {
    DoctorSlot addSlot(Long doctorId, LocalDate date, LocalTime startTime, LocalTime endTime);
    List<DoctorSlot> getAvailableSlots(Long doctorId, LocalDate date);
    List<DoctorSlot> getUpcomingSlots(Long doctorId);
    void removeSlot(Long slotId);
}
