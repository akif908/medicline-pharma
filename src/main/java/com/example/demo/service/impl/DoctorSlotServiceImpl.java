package com.example.demo.service.impl;

import com.example.demo.model.Doctor;
import com.example.demo.model.DoctorSlot;
import com.example.demo.repository.DoctorRepository;
import com.example.demo.repository.DoctorSlotRepository;
import com.example.demo.service.DoctorSlotService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DoctorSlotServiceImpl implements DoctorSlotService {

    private final DoctorSlotRepository doctorSlotRepository;
    private final DoctorRepository doctorRepository;

    @Override
    public DoctorSlot addSlot(Long doctorId, LocalDate date, LocalTime startTime, LocalTime endTime) {
        Doctor doctor = doctorRepository.findById(doctorId).orElseThrow();
        DoctorSlot slot = new DoctorSlot();
        slot.setDoctor(doctor);
        slot.setDate(date);
        slot.setStartTime(startTime);
        slot.setEndTime(endTime);
        slot.setBooked(false);
        return doctorSlotRepository.save(slot);
    }

    @Override
    public List<DoctorSlot> getAvailableSlots(Long doctorId, LocalDate date) {
        List<DoctorSlot> slots = doctorSlotRepository.findByDoctorIdAndDateOrderByStartTimeAsc(doctorId, date);
        return slots.stream().filter(s -> !s.isBooked()).toList();
    }

    @Override
    public List<DoctorSlot> getUpcomingSlots(Long doctorId) {
        return doctorSlotRepository.findByDoctorIdAndDateGreaterThanEqualOrderByDateAscStartTimeAsc(doctorId, LocalDate.now());
    }

    @Override
    public void removeSlot(Long slotId) {
        DoctorSlot slot = doctorSlotRepository.findById(slotId).orElseThrow();
        if (!slot.isBooked()) {
            doctorSlotRepository.delete(slot);
        } else {
            throw new RuntimeException("Cannot delete a booked slot");
        }
    }
}
