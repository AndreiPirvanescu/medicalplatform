package com.andrei.project.medicalplatform.dto.patient;

import lombok.*;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PatientResponseDTO {
    private Long id;
    private Long userId;
    private String email;
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private LocalDate birthDate;
    private String bloodType;
}