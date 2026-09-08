package com.andrei.project.medicalplatform.dto.medicalunit;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record MedicalUnitRequestDto(
        @NotBlank(message = "Name is required")
        @Size(max = 150, message = "Name must not exceed 150 characters")
        String name,

        @NotBlank(message = "Email is required")
        @Email(message = "Email must be valid")
        String email,

        @NotBlank(message = "Phone is required")
        @Size(max = 20, message = "Phone must not exceed 20 characters")
        String phone,

        @NotBlank(message = "Address is required")
        @Size(max = 255, message = "Address must not exceed 255 characters")
        String address,

        @Positive(message = "Manager id must be positive")
        Long managerId
) {}
