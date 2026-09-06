package com.andrei.project.medicalplatform.dto.patient;

import jakarta.validation.constraints.*;
import lombok.*;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PatientRegistrationDTO {

    @Email(message = "Email-ul nu este valid")
    @NotBlank(message = "Email-ul este obligatoriu")
    private String email;

    @NotBlank(message = "Parola este obligatorie")
    @Size(min = 6, message = "Parola trebuie să aibă cel puțin 6 caractere")
    private String password;

    @NotBlank(message = "Prenumele este obligatoriu")
    private String firstName;

    @NotBlank(message = "Numele este obligatoriu")
    private String lastName;

    @NotBlank(message = "Numărul de telefon este obligatoriu")
    @Pattern(regexp = "^[0-9]{10}$", message = "Numărul de telefon trebuie să aibă 10 cifre")
    private String phoneNumber;

    @NotNull(message = "Data nașterii este obligatorie")
    @Past(message = "Data nașterii trebuie să fie în trecut")
    private LocalDate birthDate;

    private String bloodType; // TODO:Poate rămâne opțional sau pot face un Enum
}