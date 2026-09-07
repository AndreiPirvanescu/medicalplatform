package com.andrei.project.medicalplatform.controller;

import com.andrei.project.medicalplatform.dto.prescription.PrescriptionRequestDto;
import com.andrei.project.medicalplatform.dto.prescription.PrescriptionResponseDto;
import com.andrei.project.medicalplatform.dto.prescription.PrescriptionUpdateDto;
import com.andrei.project.medicalplatform.service.PrescriptionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/prescriptions")
@RequiredArgsConstructor
public class PrescriptionController {

    private final PrescriptionService prescriptionService;

    @PostMapping("/for-patient/{patientId}/by-doctor/{doctorId}")
    public ResponseEntity<PrescriptionResponseDto> create(
            @PathVariable Long patientId,
            @PathVariable Long doctorId,
            @Valid @RequestBody PrescriptionRequestDto request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(prescriptionService.create(patientId, doctorId, request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PrescriptionResponseDto> update(
            @PathVariable Long id,
            @Valid @RequestBody PrescriptionUpdateDto request) {
        return ResponseEntity.ok(prescriptionService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        prescriptionService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<PrescriptionResponseDto> getById(@PathVariable Long id) {
        return ResponseEntity.ok(prescriptionService.getById(id));
    }

    @GetMapping
    public ResponseEntity<Page<PrescriptionResponseDto>> getAll(
            @RequestParam(required = false) Long patientId,
            @RequestParam(required = false) Long doctorId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
            @PageableDefault(size = 20, sort = "issueDate") Pageable pageable) {
        return ResponseEntity.ok(prescriptionService.getAll(patientId, doctorId, from, to, pageable));
    }

    @GetMapping("/by-patient/{patientId}")
    public ResponseEntity<List<PrescriptionResponseDto>> getByPatient(@PathVariable Long patientId) {
        return ResponseEntity.ok(prescriptionService.getByPatient(patientId));
    }
}