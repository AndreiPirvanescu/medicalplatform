package com.andrei.project.medicalplatform.controller;

import com.andrei.project.medicalplatform.dto.medicalrecord.MedicalHistoryEntryDto;
import com.andrei.project.medicalplatform.dto.medicalrecord.MedicalRecordRequestDto;
import com.andrei.project.medicalplatform.dto.medicalrecord.MedicalRecordResponseDto;
import com.andrei.project.medicalplatform.dto.medicalrecord.MedicalRecordUpdateDto;
import com.andrei.project.medicalplatform.dto.patient.PatientResponseDto;
import com.andrei.project.medicalplatform.service.MedicalRecordService;
import com.andrei.project.medicalplatform.service.PatientService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

/**
 * Medical record controller: your original JSON API (unchanged, at
 * /api/medical-records, now with explicit @ResponseBody + absolute paths -
 * same pattern as the other five controllers) plus the Thymeleaf pages for
 * Feature 6 (Medical Records Management).
 *
 * Each patient has exactly one central record (getByPatient returns a single
 * DTO, not a list), so the view flow is: GET /patients/{id}/medical-record
 * either shows the existing record + history, or - if MedicalRecordService
 * throws EntityNotFoundException, meaning none exists yet - shows a "create
 * one" prompt instead. Adjust the caught exception type if your service
 * signals "no record yet" differently.
 */
@Controller
@Validated
@RequiredArgsConstructor
public class MedicalRecordController {

    private final MedicalRecordService medicalRecordService;
    private final PatientService patientService;

    // =========================================================
    // Existing JSON API - unchanged behavior, made explicit.
    // =========================================================

    @PostMapping("/api/medical-records/for-patient/{patientId}")
    @ResponseBody
    public ResponseEntity<MedicalRecordResponseDto> create(
            @PathVariable Long patientId,
            @Valid @RequestBody MedicalRecordRequestDto request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(medicalRecordService.create(patientId, request));
    }

    @PutMapping("/api/medical-records/{id}")
    @ResponseBody
    public ResponseEntity<MedicalRecordResponseDto> update(
            @PathVariable Long id,
            @Valid @RequestBody MedicalRecordUpdateDto request) {
        return ResponseEntity.ok(medicalRecordService.update(id, request));
    }

    @GetMapping("/api/medical-records/{id}")
    @ResponseBody
    public ResponseEntity<MedicalRecordResponseDto> getById(@PathVariable Long id) {
        return ResponseEntity.ok(medicalRecordService.getById(id));
    }

    @GetMapping("/api/medical-records/by-patient/{patientId}")
    @ResponseBody
    public ResponseEntity<MedicalRecordResponseDto> getByPatient(@PathVariable Long patientId) {
        return ResponseEntity.ok(medicalRecordService.getByPatient(patientId));
    }

    @GetMapping("/api/medical-records/by-patient/{patientId}/history")
    @ResponseBody
    public ResponseEntity<List<MedicalHistoryEntryDto>> getHistory(@PathVariable Long patientId) {
        return ResponseEntity.ok(medicalRecordService.getHistory(patientId));
    }

    // =========================================================
    // New: Thymeleaf pages for Feature 6 (Medical Records Management)
    // =========================================================

    // GET /patients/{patientId}/medical-record
    @GetMapping("/patients/{patientId}/medical-record")
    public String view(@PathVariable Long patientId, Model model) {
        PatientResponseDto patient = patientService.getById(patientId);
        model.addAttribute("patient", patient);

        try {
            MedicalRecordResponseDto record = medicalRecordService.getByPatient(patientId);
            List<MedicalHistoryEntryDto> history = medicalRecordService.getHistory(patientId);
            model.addAttribute("record", record);
            model.addAttribute("history", history);
        } catch (EntityNotFoundException ex) {
            model.addAttribute("record", null);
        }

        return "medical-records/view";
    }

    // GET /patients/{patientId}/medical-record/new
    @GetMapping("/patients/{patientId}/medical-record/new")
    public String newForm(@PathVariable Long patientId, Model model) {
        model.addAttribute("patient", patientService.getById(patientId));
        return "medical-records/form";
    }

    // POST /patients/{patientId}/medical-record  (view-form equivalent of POST /api/medical-records/for-patient/{patientId})
    @PostMapping("/patients/{patientId}/medical-record")
    public String create(@PathVariable Long patientId,
                          @RequestParam(required = false) String notes,
                          RedirectAttributes redirectAttributes) {
        medicalRecordService.create(patientId, new MedicalRecordRequestDto(notes));
        redirectAttributes.addFlashAttribute("successMessage", "Medical record created.");
        return "redirect:/patients/" + patientId + "/medical-record";
    }

    // GET /medical-records/{id}/edit?patientId=  (patientId threaded through so we
    // know where to redirect back to and can show the patient's name)
    @GetMapping("/medical-records/{id}/edit")
    public String editForm(@PathVariable Long id, @RequestParam Long patientId, Model model) {
        model.addAttribute("record", medicalRecordService.getById(id));
        model.addAttribute("patient", patientService.getById(patientId));
        return "medical-records/edit";
    }

    // POST /medical-records/{id}/update  (view-form equivalent of PUT /api/medical-records/{id})
    @PostMapping("/medical-records/{id}/update")
    public String updateFromForm(@PathVariable Long id,
                                  @RequestParam Long patientId,
                                  @RequestParam @jakarta.validation.constraints.NotBlank String notes,
                                  RedirectAttributes redirectAttributes) {
        medicalRecordService.update(id, new MedicalRecordUpdateDto(notes));
        redirectAttributes.addFlashAttribute("successMessage", "Medical record updated.");
        return "redirect:/patients/" + patientId + "/medical-record";
    }
}
