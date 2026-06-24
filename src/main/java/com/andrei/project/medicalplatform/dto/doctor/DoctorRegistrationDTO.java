package com.andrei.project.medicalplatform.dto.doctor;

import com.andrei.project.medicalplatform.model.Specialization;
import jakarta.validation.constraints.*;
import lombok.*;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DoctorRegistrationDTO {

    @Email(message = "Email-ul nu este valid")
    @NotBlank(message = "Email-ul este obligatoriu")
    private String email;

    @NotBlank(message = "Parola este obligatorie")
    @Size(min = 6, message = "Parola trebuie să aibă cel putin 6 caractere")
    private String password;

    @NotBlank(message = "Prenumele este obligatoriu")
    private String firstName;

    @NotBlank(message = "Numele este obligatoriu")
    private String lastName;

    @NotEmpty(message = "Doctorul trebuie sa aiba cel putin o specializare")
    private List<Specialization> specializations;

    @NotBlank(message = "Codul de licenta este obligatoriu")
    private String licenseNumber;
}