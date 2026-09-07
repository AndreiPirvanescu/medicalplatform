package com.andrei.project.medicalplatform.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "medications")
@NoArgsConstructor @AllArgsConstructor @Getter @Setter
public class Medication {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private String dosage;

    @Column(columnDefinition = "TEXT")
    private String description;

    @ManyToMany(mappedBy = "medications")
    private List<Prescription> prescriptions;
}