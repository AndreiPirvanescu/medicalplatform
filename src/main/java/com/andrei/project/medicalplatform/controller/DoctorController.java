package com.andrei.project.medicalplatform.controller;

import com.andrei.project.medicalplatform.dto.doctor.DoctorRequestDto;
import com.andrei.project.medicalplatform.dto.doctor.DoctorResponseDto;
import com.andrei.project.medicalplatform.model.Specialization;
import com.andrei.project.medicalplatform.service.DoctorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/doctors")
@RequiredArgsConstructor
public class DoctorController {

    private final DoctorService doctorService;

    @PutMapping("/{id}")
    public ResponseEntity<DoctorResponseDto> update(
            @PathVariable Long id,
            @Valid @RequestBody DoctorRequestDto request) {
        return ResponseEntity.ok(doctorService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        doctorService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<DoctorResponseDto> getById(@PathVariable Long id) {
        return ResponseEntity.ok(doctorService.getById(id));
    }

    @GetMapping
    public ResponseEntity<Page<DoctorResponseDto>> getAll(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Specialization specialization,
            @RequestParam(required = false) Long medicalUnitId,
            @PageableDefault(size = 20, sort = "id") Pageable pageable) {
        return ResponseEntity.ok(doctorService.getAll(name, specialization, medicalUnitId, pageable));
    }

    @PostMapping("/{id}/specializations/{specId}")
    public ResponseEntity<DoctorResponseDto> addSpecialization(
            @PathVariable Long id, @PathVariable Specialization specId) {
        return ResponseEntity.ok(doctorService.addSpecialization(id, specId));
    }

    @DeleteMapping("/{id}/specializations/{specId}")
    public ResponseEntity<DoctorResponseDto> removeSpecialization(
            @PathVariable Long id, @PathVariable Specialization specId) {
        return ResponseEntity.ok(doctorService.removeSpecialization(id, specId));
    }
}