package com.andrei.project.medicalplatform.web.form;

import com.andrei.project.medicalplatform.model.Specialization;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.ArrayList;
import java.util.List;

/**
 * Mutable form-backing bean for the doctor create/edit page.
 * See MedicalUnitFormDto for why this doesn't reuse DoctorRequestDto directly
 * (that record stays exactly as-is for the JSON API).
 */
public class DoctorFormDto {

    private Long id;
    private Long medicalUnitId; // only used on create, to know which unit this doctor is added to

    @NotNull(message = "User is required")
    private Long userId;

    @NotBlank(message = "License number is required")
    @Size(max = 50, message = "License number must not exceed 50 characters")
    private String licenseNumber;

    @NotEmpty(message = "At least one specialization is required")
    private List<Specialization> specializations = new ArrayList<>();

    public DoctorFormDto() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getMedicalUnitId() { return medicalUnitId; }
    public void setMedicalUnitId(Long medicalUnitId) { this.medicalUnitId = medicalUnitId; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getLicenseNumber() { return licenseNumber; }
    public void setLicenseNumber(String licenseNumber) { this.licenseNumber = licenseNumber; }

    public List<Specialization> getSpecializations() { return specializations; }
    public void setSpecializations(List<Specialization> specializations) { this.specializations = specializations; }
}
