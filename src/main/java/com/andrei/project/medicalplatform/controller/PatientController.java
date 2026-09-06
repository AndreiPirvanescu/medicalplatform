package com.andrei.project.medicalplatform.controller;

import com.andrei.project.medicalplatform.dto.patient.PatientRegistrationDTO;
import com.andrei.project.medicalplatform.dto.patient.PatientResponseDTO;
import com.andrei.project.medicalplatform.service.PatientService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/patients")
@RequiredArgsConstructor
public class PatientController {

    private final PatientService patientService;

    @PostMapping("/register")
    public ResponseEntity<PatientResponseDTO> registerPatient(@Valid @RequestBody PatientRegistrationDTO dto) {
        PatientResponseDTO createdPatient = patientService.registerPatient(dto);
        return new ResponseEntity<>(createdPatient, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<PatientResponseDTO>> getAllPatients() {
        return ResponseEntity.ok(patientService.getAllPatients());
    }

    @GetMapping("/{id}")
    public ResponseEntity<PatientResponseDTO> getPatientById(@PathVariable Long id) {
        return ResponseEntity.ok(patientService.getPatientById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PatientResponseDTO> updatePatient(@PathVariable Long id, @Valid @RequestBody PatientRegistrationDTO dto) {
        return ResponseEntity.ok(patientService.updatePatient(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePatient(@PathVariable Long id) {
        patientService.deletePatient(id);
        return ResponseEntity.noContent().build();
    }
}