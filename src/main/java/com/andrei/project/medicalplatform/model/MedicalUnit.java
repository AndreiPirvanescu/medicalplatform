package com.andrei.project.medicalplatform.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "medical_units")
@NoArgsConstructor @AllArgsConstructor @Getter @Setter
public class MedicalUnit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Column(unique = true, nullable = false, length = 100)
    private String email;

    private String phone;

    private String address;

    @OneToOne
    @JoinColumn(name = "manager_id", nullable =false, unique = true)
    private User manager;

    @OneToMany(mappedBy = "medicalUnit")
    private List<Doctor> doctors;
}