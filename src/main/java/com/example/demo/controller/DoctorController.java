package com.example.demo.controller;

import com.example.demo.model.Doctor;
import com.example.demo.service.DoctorService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/doctors")
@RequiredArgsConstructor
public class DoctorController {

    private final DoctorService doctorService;

    @GetMapping
    public List<Doctor> getAllDoctors() {
        return doctorService.getAllDoctors();
    }

    @GetMapping("/{id}")
    public Doctor getDoctor(@PathVariable Long id) {
        return doctorService.getDoctorById(id).orElse(null);
    }

    @PutMapping("/{id}")
    public Doctor updateDoctor(@PathVariable Long id, @RequestBody Doctor updatedDetails) {
        Doctor doctor = doctorService.getDoctorById(id).orElseThrow();
        if (updatedDetails.getSpecialization() != null) doctor.setSpecialization(updatedDetails.getSpecialization());
        if (updatedDetails.getBio() != null) doctor.setBio(updatedDetails.getBio());
        if (updatedDetails.getExperience() > 0) doctor.setExperience(updatedDetails.getExperience());
        return doctorService.saveDoctor(doctor);
    }
}
