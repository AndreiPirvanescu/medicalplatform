package com.andrei.project.medicalplatform.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "medical_records")
@NoArgsConstructor @AllArgsConstructor @Getter @Setter
public class MedicalRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "patient_id", unique = true, nullable = false)
    private Patient patient;

    private LocalDateTime createdDate;

    @Column(columnDefinition = "TEXT")
    private String notes;
}