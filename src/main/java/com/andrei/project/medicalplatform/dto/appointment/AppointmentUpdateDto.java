package com.andrei.project.medicalplatform.dto.appointment;

import com.andrei.project.medicalplatform.model.AppointmentStatus;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record AppointmentUpdateDto(
        @NotNull(message = "Appointment date and time is required")
        LocalDateTime appointmentDateTime,

        @NotNull(message = "Status is required")
        AppointmentStatus status
) {}