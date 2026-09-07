package com.andrei.project.medicalplatform.controller;

import com.andrei.project.medicalplatform.dto.medication.MedicationHistoryEntryDto;
import com.andrei.project.medicalplatform.dto.medication.MedicationResponseDto;
import com.andrei.project.medicalplatform.dto.patient.PatientResponseDto;
import com.andrei.project.medicalplatform.service.MedicationService;
import com.andrei.project.medicalplatform.service.PatientService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Medication controller: your original JSON API (unchanged, at
 * /api/medications, now with explicit @ResponseBody + absolute paths - same
 * pattern as the other controllers) plus the Thymeleaf pages for Feature 8
 * (Medication & Treatment History).
 *
 * Your API has no create/update/delete for medications themselves (they're
 * only added to/removed from prescriptions, per Feature 7's
 * PrescriptionController) - so this is a read-only catalog + history
 * viewer, no forms.
 */
@Controller
@RequiredArgsConstructor
public class MedicationController {

    private final MedicationService medicationService;
    private final PatientService patientService;

    // =========================================================
    // Existing JSON API - unchanged behavior, made explicit.
    // =========================================================

    @GetMapping("/api/medications")
    @ResponseBody
    public ResponseEntity<Page<MedicationResponseDto>> getAll(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Long patientId,
            @PageableDefault(size = 20, sort = "name") Pageable pageable) {
        return ResponseEntity.ok(medicationService.getAll(name, patientId, pageable));
    }

    @GetMapping("/api/medications/{id}")
    @ResponseBody
    public ResponseEntity<MedicationResponseDto> getById(@PathVariable Long id) {
        return ResponseEntity.ok(medicationService.getById(id));
    }

    @GetMapping("/api/medications/by-patient/{patientId}/history")
    @ResponseBody
    public ResponseEntity<List<MedicationHistoryEntryDto>> getPatientHistory(
            @PathVariable Long patientId,
            @RequestParam(required = false) String name) {
        return ResponseEntity.ok(medicationService.getPatientHistory(patientId, name));
    }

    // =========================================================
    // New: Thymeleaf pages for Feature 8 (Medication & Treatment History)
    // =========================================================

    // GET /medications?name=&page=&size=&sort=
    @GetMapping("/medications")
    public String list(@RequestParam(required = false) String name,
                        @PageableDefault(size = 20, sort = "name") Pageable pageable,
                        Model model) {
        Page<MedicationResponseDto> medications = medicationService.getAll(name, null, pageable);
        model.addAttribute("medications", medications);
        return "medications/list";
    }

    // GET /medications/{id}
    @GetMapping("/medications/{id}")
    public String view(@PathVariable Long id, Model model) {
        model.addAttribute("medication", medicationService.getById(id));
        return "medications/view";
    }

    // GET /patients/{patientId}/medication-history?name=
    @GetMapping("/patients/{patientId}/medication-history")
    public String patientHistory(@PathVariable Long patientId,
                                  @RequestParam(required = false) String name,
                                  Model model) {
        PatientResponseDto patient = patientService.getById(patientId);
        List<MedicationHistoryEntryDto> history = medicationService.getPatientHistory(patientId, name);

        model.addAttribute("patient", patient);
        model.addAttribute("history", history);
        model.addAttribute("nameFilter", name);
        return "medications/patient-history";
    }
}
