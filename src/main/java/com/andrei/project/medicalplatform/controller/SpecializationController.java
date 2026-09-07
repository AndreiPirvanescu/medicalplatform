package com.andrei.project.medicalplatform.controller;

import com.andrei.project.medicalplatform.dto.doctor.DoctorResponseDto;
import com.andrei.project.medicalplatform.dto.specialization.SpecializationDto;
import com.andrei.project.medicalplatform.model.Specialization;
import com.andrei.project.medicalplatform.service.SpecializationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/specializations")
@RequiredArgsConstructor
public class SpecializationController {

    private final SpecializationService specializationService;

    @GetMapping
    public ResponseEntity<List<SpecializationDto>> getAll() {
        return ResponseEntity.ok(specializationService.getAll());
    }

    @GetMapping("/{id}/doctors")
    public ResponseEntity<List<DoctorResponseDto>> getDoctorsBySpecialization(@PathVariable Specialization id) {
        return ResponseEntity.ok(specializationService.getDoctorsBySpecialization(id));
    }
}