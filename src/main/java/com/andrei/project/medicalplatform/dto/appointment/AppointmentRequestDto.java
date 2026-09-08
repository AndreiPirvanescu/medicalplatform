package com.andrei.project.medicalplatform.dto.appointment;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record AppointmentRequestDto(
        @NotNull(message = "Appointment date and time is required")
        @Future(message = "Appointment date and time must be in the future")
        LocalDateTime appointmentDateTime
) {}