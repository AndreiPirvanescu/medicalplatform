package com.andrei.project.medicalplatform.controller;

import com.andrei.project.medicalplatform.dto.appointment.AppointmentRequestDto;
import com.andrei.project.medicalplatform.dto.appointment.AppointmentResponseDto;
import com.andrei.project.medicalplatform.dto.appointment.AppointmentUpdateDto;
import com.andrei.project.medicalplatform.model.AppointmentStatus;
import com.andrei.project.medicalplatform.service.AppointmentService;
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
@RequestMapping("/api/appointments")
@RequiredArgsConstructor
public class AppointmentController {

    private final AppointmentService appointmentService;

    @PostMapping("/for-patient/{patientId}/with-doctor/{doctorId}")
    public ResponseEntity<AppointmentResponseDto> book(
            @PathVariable Long patientId,
            @PathVariable Long doctorId,
            @Valid @RequestBody AppointmentRequestDto request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(appointmentService.create(patientId, doctorId, request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<AppointmentResponseDto> update(
            @PathVariable Long id,
            @Valid @RequestBody AppointmentUpdateDto request) {
        return ResponseEntity.ok(appointmentService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        appointmentService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<AppointmentResponseDto> getById(@PathVariable Long id) {
        return ResponseEntity.ok(appointmentService.getById(id));
    }

    @GetMapping
    public ResponseEntity<Page<AppointmentResponseDto>> getAll(
            @RequestParam(required = false) Long patientId,
            @RequestParam(required = false) Long doctorId,
            @RequestParam(required = false) AppointmentStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
            @PageableDefault(size = 20, sort = "appointmentDateTime") Pageable pageable) {
        return ResponseEntity.ok(appointmentService.getAll(patientId, doctorId, status, from, to, pageable));
    }

    @GetMapping("/by-doctor/{doctorId}")
    public ResponseEntity<List<AppointmentResponseDto>> getByDoctor(@PathVariable Long doctorId) {
        return ResponseEntity.ok(appointmentService.getByDoctor(doctorId));
    }

    @GetMapping("/by-patient/{patientId}")
    public ResponseEntity<List<AppointmentResponseDto>> getByPatient(@PathVariable Long patientId) {
        return ResponseEntity.ok(appointmentService.getByPatient(patientId));
    }
}