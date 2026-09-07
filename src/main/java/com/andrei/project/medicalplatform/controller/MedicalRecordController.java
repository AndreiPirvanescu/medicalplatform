package com.andrei.project.medicalplatform.controller;

import com.andrei.project.medicalplatform.dto.medicalrecord.MedicalHistoryEntryDto;
import com.andrei.project.medicalplatform.dto.medicalrecord.MedicalRecordRequestDto;
import com.andrei.project.medicalplatform.dto.medicalrecord.MedicalRecordResponseDto;
import com.andrei.project.medicalplatform.dto.medicalrecord.MedicalRecordUpdateDto;
import com.andrei.project.medicalplatform.service.MedicalRecordService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/medical-records")
@RequiredArgsConstructor
public class MedicalRecordController {

    private final MedicalRecordService medicalRecordService;

    @PostMapping("/for-patient/{patientId}")
    public ResponseEntity<MedicalRecordResponseDto> create(
            @PathVariable Long patientId,
            @Valid @RequestBody MedicalRecordRequestDto request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(medicalRecordService.create(patientId, request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<MedicalRecordResponseDto> update(
            @PathVariable Long id,
            @Valid @RequestBody MedicalRecordUpdateDto request) {
        return ResponseEntity.ok(medicalRecordService.update(id, request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<MedicalRecordResponseDto> getById(@PathVariable Long id) {
        return ResponseEntity.ok(medicalRecordService.getById(id));
    }

    @GetMapping("/by-patient/{patientId}")
    public ResponseEntity<MedicalRecordResponseDto> getByPatient(@PathVariable Long patientId) {
        return ResponseEntity.ok(medicalRecordService.getByPatient(patientId));
    }

    @GetMapping("/by-patient/{patientId}/history")
    public ResponseEntity<List<MedicalHistoryEntryDto>> getHistory(@PathVariable Long patientId) {
        return ResponseEntity.ok(medicalRecordService.getHistory(patientId));
    }
}