package com.andrei.project.medicalplatform.dto.doctor;

import com.andrei.project.medicalplatform.model.Specialization;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

public record DoctorCreateRequestDTO (
    @NotBlank(message = "First name is required.")
    String firstName,

    @NotBlank(message = "Last name is required.")
    String lastName,

    @Email(message = "The email format is incorrect.")
    @NotBlank(message = "Email is required.")
    String email,

    @Size(min = 6, message = "The password must be at least 6 characters long.")
    @NotBlank(message = "Password is required.")
    String password,

    @NotEmpty(message = "At least one specialization is required for a doctor.")
    List<Specialization> specializations,

    @NotBlank(message = "The license code is required.")
    String licenseNumber
){}