package com.andrei.project.medicalplatform.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "prescriptions")
@NoArgsConstructor @AllArgsConstructor @Getter @Setter
public class Prescription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id")
    private Patient patient;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doctor_id")
    private Doctor doctor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "appointment_id")
    private Appointment appointment;

    private LocalDateTime issueDate;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @ManyToMany
    @JoinTable(name = "prescription_medications",
            joinColumns = @JoinColumn(name = "prescription_id"),
            inverseJoinColumns = @JoinColumn(name = "medication_id"))
    private List<Medication> medications;
}