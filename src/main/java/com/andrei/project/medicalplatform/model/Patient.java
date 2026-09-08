package com.andrei.project.medicalplatform.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "patients")
@NoArgsConstructor @AllArgsConstructor @Getter @Setter
public class Patient {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @Column(name = "blood_type")
    private String bloodType;

    @OneToOne(mappedBy = "patient", cascade = CascadeType.ALL, orphanRemoval = true)
    private MedicalRecord medicalRecords;

    @OneToMany(mappedBy = "patient", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Appointment> appointment;
}