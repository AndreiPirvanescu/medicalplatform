package com.andrei.project.medicalplatform.controller;

import com.andrei.project.medicalplatform.dto.schedule.ScheduleRequestDto;
import com.andrei.project.medicalplatform.dto.schedule.ScheduleResponseDto;
import com.andrei.project.medicalplatform.dto.schedule.ScheduleUpdateDto;
import com.andrei.project.medicalplatform.service.ScheduleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/schedules")
@RequiredArgsConstructor
public class ScheduleController {

    private final ScheduleService scheduleService;

    @PostMapping("/for-doctor/{doctorId}")
    public ResponseEntity<ScheduleResponseDto> create(
            @PathVariable Long doctorId,
            @Valid @RequestBody ScheduleRequestDto request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(scheduleService.create(doctorId, request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ScheduleResponseDto> update(
            @PathVariable Long id,
            @Valid @RequestBody ScheduleUpdateDto request) {
        return ResponseEntity.ok(scheduleService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        scheduleService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/by-doctor/{doctorId}")
    public ResponseEntity<List<ScheduleResponseDto>> getByDoctor(@PathVariable Long doctorId) {
        return ResponseEntity.ok(scheduleService.getByDoctor(doctorId));
    }

    @GetMapping("/by-doctor/{doctorId}/available")
    public ResponseEntity<List<ScheduleResponseDto>> getAvailable(
            @PathVariable Long doctorId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to) {
        return ResponseEntity.ok(scheduleService.getAvailableByDoctor(doctorId, from, to));
    }
}