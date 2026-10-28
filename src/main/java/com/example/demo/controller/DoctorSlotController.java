package com.example.demo.controller;

import com.example.demo.model.DoctorSlot;
import com.example.demo.service.DoctorSlotService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/slots")
@RequiredArgsConstructor
public class DoctorSlotController {

    private final DoctorSlotService doctorSlotService;

    @PostMapping
    public DoctorSlot addSlot(@RequestBody Map<String, String> payload) {
        Long doctorId = Long.parseLong(payload.get("doctorId"));
        LocalDate date = LocalDate.parse(payload.get("date"));
        LocalTime startTime = LocalTime.parse(payload.get("startTime"));
        LocalTime endTime = LocalTime.parse(payload.get("endTime"));
        return doctorSlotService.addSlot(doctorId, date, startTime, endTime);
    }

    @GetMapping("/available")
    public List<DoctorSlot> getAvailableSlots(
            @RequestParam Long doctorId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return doctorSlotService.getAvailableSlots(doctorId, date);
    }

    @GetMapping("/upcoming/{doctorId}")
    public List<DoctorSlot> getUpcomingSlots(@PathVariable Long doctorId) {
        return doctorSlotService.getUpcomingSlots(doctorId);
    }

    @DeleteMapping("/{slotId}")
    public void removeSlot(@PathVariable Long slotId) {
        doctorSlotService.removeSlot(slotId);
    }
}
