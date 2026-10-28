package com.example.demo.service;

import com.example.demo.model.Appointment;
import java.util.List;

public interface AppointmentService {
    Appointment createAppointment(Long patientId, Long doctorSlotId, String reason);
    List<Appointment> getPatientAppointments(Long patientId);
    List<Appointment> getDoctorAppointments(Long doctorId);
    Appointment approveAppointment(Long appointmentId);
    Appointment rejectAppointment(Long appointmentId);
    Appointment completeAppointment(Long appointmentId);
}
