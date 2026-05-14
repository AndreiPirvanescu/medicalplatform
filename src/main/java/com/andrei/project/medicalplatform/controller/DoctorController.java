package com.andrei.project.medicalplatform.controller;

import com.andrei.project.medicalplatform.dto.CreateDoctorRequestDTO;
import com.andrei.project.medicalplatform.dto.DoctorResponseDTO;
import com.andrei.project.medicalplatform.dto.UpdateDoctorRequestDTO;
import com.andrei.project.medicalplatform.service.DoctorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/doctors")
@RequiredArgsConstructor
@Tag(name = "Doctors", description = "Operations related to doctors")
public class DoctorController {

    private final DoctorService doctorService;

    @PostMapping
    @Operation(summary = "Create doctor")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Doctor created successfully",
                    content = @Content(schema = @Schema(implementation = DoctorResponseDTO.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid request",
                    content = @Content
            )
    })
    public ResponseEntity<DoctorResponseDTO> createDoctor(
            @Valid @RequestBody CreateDoctorRequestDTO dto
    ) {

        DoctorResponseDTO response = doctorService.createDoctor(dto);

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping
    @Operation(summary = "Get all doctors")
    public ResponseEntity<List<DoctorResponseDTO>> getAllDoctors() {

        return ResponseEntity.ok(doctorService.getAllDoctors());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get doctor by id")
    public ResponseEntity<DoctorResponseDTO> getDoctorById(
            @PathVariable Long id
    ) {

        return ResponseEntity.ok(doctorService.getDoctorById(id));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update doctor")
    public ResponseEntity<DoctorResponseDTO> updateDoctor(
            @PathVariable Long id,
            @Valid @RequestBody UpdateDoctorRequestDTO dto
    ) {

        return ResponseEntity.ok(
                doctorService.updateDoctor(id, dto)
        );
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete doctor")
    public ResponseEntity<Void> deleteDoctor(
            @PathVariable Long id
    ) {

        doctorService.deleteDoctor(id);

        return ResponseEntity.noContent().build();
    }
}