package com.andrei.project.medicalplatform.web.form;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

/**
 * Mutable form-backing bean for the patient register/edit page.
 * See MedicalUnitFormDto for why this doesn't reuse PatientRequestDto directly.
 */
public class PatientFormDto {

    private Long id;

    @NotNull(message = "User is required")
    private Long userId;

    @NotNull(message = "Date of birth is required")
    @Past(message = "Date of birth must be in the past")
    private LocalDate dateOfBirth;

    @NotBlank(message = "Blood type is required")
    @Size(max = 5, message = "Blood type must not exceed 5 characters")
    private String bloodType;

    public PatientFormDto() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public LocalDate getDateOfBirth() { return dateOfBirth; }
    public void setDateOfBirth(LocalDate dateOfBirth) { this.dateOfBirth = dateOfBirth; }

    public String getBloodType() { return bloodType; }
    public void setBloodType(String bloodType) { this.bloodType = bloodType; }
}
