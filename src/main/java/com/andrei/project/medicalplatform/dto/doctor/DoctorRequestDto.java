package com.andrei.project.medicalplatform.dto.doctor;

import com.andrei.project.medicalplatform.model.Specialization;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.util.List;

public record DoctorRequestDto(
        @NotNull(message = "User id is required")
        @Positive(message = "User id must be positive")
        Long userId,

        @NotBlank(message = "License number is required")
        @Size(max = 50, message = "License number must not exceed 50 characters")
        String licenseNumber,

        @NotEmpty(message = "At least one specialization is required")
        List<@NotNull Specialization> specializations
) {}