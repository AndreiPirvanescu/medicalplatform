package com.andrei.project.medicalplatform.controller;

import com.andrei.project.medicalplatform.dto.medicalunit.MedicalUnitRequestDto;
import com.andrei.project.medicalplatform.dto.medicalunit.MedicalUnitResponseDto;
import com.andrei.project.medicalplatform.service.MedicalUnitService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/medicalUnits")
@RequiredArgsConstructor
public class MedicalUnitController {

    private final MedicalUnitService medicalUnitService;

    @PostMapping
    public ResponseEntity<MedicalUnitResponseDto> create(@Valid @RequestBody MedicalUnitRequestDto request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(medicalUnitService.create(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<MedicalUnitResponseDto> update(
            @PathVariable Long id,
            @Valid @RequestBody MedicalUnitRequestDto request) {
        return ResponseEntity.ok(medicalUnitService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        medicalUnitService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<MedicalUnitResponseDto> getById(@PathVariable Long id) {
        return ResponseEntity.ok(medicalUnitService.getById(id));
    }

    @GetMapping("/by-manager/{managerId}")
    public ResponseEntity<MedicalUnitResponseDto> getByManager(@PathVariable Long managerId) {
        return ResponseEntity.ok(medicalUnitService.getByManagerId(managerId));
    }

    @GetMapping
    public ResponseEntity<Page<MedicalUnitResponseDto>> getAll(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String location,
            @PageableDefault(size = 20, sort = "name") Pageable pageable) {
        return ResponseEntity.ok(medicalUnitService.getAll(name, location, pageable));
    }
}