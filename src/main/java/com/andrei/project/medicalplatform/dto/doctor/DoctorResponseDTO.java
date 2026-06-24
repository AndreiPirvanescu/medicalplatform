package com.andrei.project.medicalplatform.dto.doctor;

import com.andrei.project.medicalplatform.model.Specialization;
import lombok.*;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DoctorResponseDTO {
    private Long id;
    private Long userId;
    private String email;
    private String firstName;
    private String lastName;
    private List<Specialization> specializations;
    private String licenseNumber;
}