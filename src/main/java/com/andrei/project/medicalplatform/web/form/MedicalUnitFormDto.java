package com.andrei.project.medicalplatform.web.form;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Mutable form-backing bean for the medical unit create/edit page.
 *
 * Thymeleaf's th:field two-way binding (and Spring's classic WebDataBinder)
 * work most reliably against plain JavaBeans (getters + setters + no-args
 * constructor) rather than the immutable records used for the JSON API DTOs -
 * that's why this is a separate class instead of reusing
 * MedicalUnitRequestDto/MedicalUnitResponseDto directly. The controller
 * converts to/from your real API DTOs at the service boundary.
 *
 * "id" is null for the create form and populated for the edit form - the
 * templates branch on that to decide labels/targets.
 */
public class MedicalUnitFormDto {

    private Long id;

    @NotBlank(message = "Name is required")
    @Size(max = 150, message = "Name must not exceed 150 characters")
    private String name;

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    private String email;

    @NotBlank(message = "Phone is required")
    @Size(max = 20, message = "Phone must not exceed 20 characters")
    private String phone;

    @NotBlank(message = "Address is required")
    @Size(max = 255, message = "Address must not exceed 255 characters")
    private String address;

    private Long managerId;

    public MedicalUnitFormDto() {}

    public static MedicalUnitFormDto empty() {
        return new MedicalUnitFormDto();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public Long getManagerId() { return managerId; }
    public void setManagerId(Long managerId) { this.managerId = managerId; }
}
