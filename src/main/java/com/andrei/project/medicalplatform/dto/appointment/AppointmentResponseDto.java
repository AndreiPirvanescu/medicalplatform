package com.andrei.project.medicalplatform.dto.appointment;

import com.andrei.project.medicalplatform.model.AppointmentStatus;

import java.time.LocalDateTime;

public record AppointmentResponseDto(
        Long id,
        Long patientId,
        String patientFullName,
        Long doctorId,
        String doctorFullName,
        LocalDateTime appointmentDateTime,
        AppointmentStatus status
) {}