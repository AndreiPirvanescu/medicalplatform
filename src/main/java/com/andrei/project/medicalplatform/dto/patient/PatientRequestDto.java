package com.andrei.project.medicalplatform.dto.patient;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record PatientRequestDto(
        @NotNull(message = "User id is required")
        @Positive(message = "User id must be positive")
        Long userId,

        @NotNull(message = "Date of birth is required")
        @Past(message = "Date of birth must be in the past")
        LocalDate dateOfBirth,

        @NotBlank(message = "Blood type is required")
        @Size(max = 5, message = "Blood type must not exceed 5 characters")
        String bloodType
) {}