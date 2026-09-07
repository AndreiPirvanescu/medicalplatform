package com.andrei.project.medicalplatform.controller;

import com.andrei.project.medicalplatform.dto.medication.MedicationHistoryEntryDto;
import com.andrei.project.medicalplatform.dto.medication.MedicationResponseDto;
import com.andrei.project.medicalplatform.service.MedicationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/medications")
@RequiredArgsConstructor
public class MedicationController {

    private final MedicationService medicationService;

    @GetMapping
    public ResponseEntity<Page<MedicationResponseDto>> getAll(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Long patientId,
            @PageableDefault(size = 20, sort = "name") Pageable pageable) {
        return ResponseEntity.ok(medicationService.getAll(name, patientId, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<MedicationResponseDto> getById(@PathVariable Long id) {
        return ResponseEntity.ok(medicationService.getById(id));
    }

    @GetMapping("/by-patient/{patientId}/history")
    public ResponseEntity<List<MedicationHistoryEntryDto>> getPatientHistory(
            @PathVariable Long patientId,
            @RequestParam(required = false) String name) {
        return ResponseEntity.ok(medicationService.getPatientHistory(patientId, name));
    }
}