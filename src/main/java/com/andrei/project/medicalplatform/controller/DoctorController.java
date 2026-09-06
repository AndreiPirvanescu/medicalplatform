package com.andrei.project.medicalplatform.controller;

import com.andrei.project.medicalplatform.dto.doctor.DoctorCreateRequestDTO;
import com.andrei.project.medicalplatform.dto.doctor.DoctorResponseDTO;
import com.andrei.project.medicalplatform.service.DoctorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/doctors")
public class DoctorController {

    private final DoctorService doctorService;

    @GetMapping
    public ResponseEntity<List<DoctorResponseDTO>> getAllDoctors() {
        List<DoctorResponseDTO> doctors = doctorService.getAllDoctors();
        return ResponseEntity.ok(doctors);
    }

    @GetMapping("/{id}")
    public ResponseEntity<DoctorResponseDTO> getDoctorById(@PathVariable Long id) {
        DoctorResponseDTO doctor = doctorService.getDoctorById(id);
        return ResponseEntity.ok(doctor);
    }

    @PostMapping
    public ResponseEntity<DoctorResponseDTO> createDoctor(@Valid @RequestBody DoctorCreateRequestDTO doctorCreateRequestDto) {
        DoctorResponseDTO createdDoctor = doctorService.addDoctor(doctorCreateRequestDto);
        return new ResponseEntity<>(createdDoctor, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<DoctorResponseDTO> updateDoctor(@PathVariable Long id, @Valid @RequestBody DoctorCreateRequestDTO dto) {
        DoctorResponseDTO updatedDoctor = doctorService.updateDoctor(id, dto);
        return ResponseEntity.ok(updatedDoctor);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteDoctor(@PathVariable Long id) {
        doctorService.deleteDoctor(id);
        return ResponseEntity.noContent().build();
    }
}