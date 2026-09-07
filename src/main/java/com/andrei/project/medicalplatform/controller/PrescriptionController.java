package com.andrei.project.medicalplatform.controller;

import com.andrei.project.medicalplatform.dto.appointment.AppointmentResponseDto;
import com.andrei.project.medicalplatform.dto.medication.MedicationResponseDto;
import com.andrei.project.medicalplatform.dto.prescription.PrescriptionRequestDto;
import com.andrei.project.medicalplatform.dto.prescription.PrescriptionResponseDto;
import com.andrei.project.medicalplatform.dto.prescription.PrescriptionUpdateDto;
import com.andrei.project.medicalplatform.service.AppointmentService;
import com.andrei.project.medicalplatform.service.DoctorService;
import com.andrei.project.medicalplatform.service.MedicationService;
import com.andrei.project.medicalplatform.service.PatientService;
import com.andrei.project.medicalplatform.service.PrescriptionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Prescription controller: your original JSON API (unchanged, at
 * /api/prescriptions, now with explicit @ResponseBody + absolute paths -
 * same pattern as the other controllers) plus the Thymeleaf pages for
 * Feature 7 (Prescription Management).
 *
 * PatientService/DoctorService/AppointmentService/MedicationService are all
 * injected purely to populate dropdowns/checkboxes on the create/edit forms -
 * they already exist for earlier features.
 *
 * Two ways to change a prescription's medications exist in your API, and
 * both are wired up here:
 *   - PUT /api/prescriptions/{id} (PrescriptionUpdateDto) replaces the whole
 *     medication list at once - used by the "Edit" page (checkboxes).
 *   - POST/DELETE .../medications/{medicationId} add or remove one at a time -
 *     used by small inline forms on the "View" page.
 */
@Controller
@RequiredArgsConstructor
public class PrescriptionController {

    private final PrescriptionService prescriptionService;
    private final MedicationService medicationService;
    private final PatientService patientService;
    private final DoctorService doctorService;
    private final AppointmentService appointmentService;

    @ModelAttribute("allPatients")
    public List<com.andrei.project.medicalplatform.dto.patient.PatientResponseDto> allPatients() {
        return patientService.getAll(null, null, PageRequest.of(0, 500)).getContent();
    }

    @ModelAttribute("allDoctors")
    public List<com.andrei.project.medicalplatform.dto.doctor.DoctorResponseDto> allDoctors() {
        return doctorService.getAll(null, null, null, PageRequest.of(0, 500)).getContent();
    }

    @ModelAttribute("allMedications")
    public List<MedicationResponseDto> allMedications() {
        return medicationService.getAll(null, null, PageRequest.of(0, 500)).getContent();
    }

    // =========================================================
    // Existing JSON API - unchanged behavior, made explicit.
    // =========================================================

    @PostMapping("/api/prescriptions/for-patient/{patientId}/by-doctor/{doctorId}")
    @ResponseBody
    public ResponseEntity<PrescriptionResponseDto> create(
            @PathVariable Long patientId,
            @PathVariable Long doctorId,
            @Valid @RequestBody PrescriptionRequestDto request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(prescriptionService.create(patientId, doctorId, request));
    }

    @PutMapping("/api/prescriptions/{id}")
    @ResponseBody
    public ResponseEntity<PrescriptionResponseDto> update(
            @PathVariable Long id,
            @Valid @RequestBody PrescriptionUpdateDto request) {
        return ResponseEntity.ok(prescriptionService.update(id, request));
    }

    @DeleteMapping("/api/prescriptions/{id}")
    @ResponseBody
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        prescriptionService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/api/prescriptions/{id}")
    @ResponseBody
    public ResponseEntity<PrescriptionResponseDto> getById(@PathVariable Long id) {
        return ResponseEntity.ok(prescriptionService.getById(id));
    }

    @GetMapping("/api/prescriptions")
    @ResponseBody
    public ResponseEntity<Page<PrescriptionResponseDto>> getAll(
            @RequestParam(required = false) Long patientId,
            @RequestParam(required = false) Long doctorId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
            @PageableDefault(size = 20, sort = "issueDate") Pageable pageable) {
        return ResponseEntity.ok(prescriptionService.getAll(patientId, doctorId, from, to, pageable));
    }

    @GetMapping("/api/prescriptions/by-patient/{patientId}")
    @ResponseBody
    public ResponseEntity<List<PrescriptionResponseDto>> getByPatient(@PathVariable Long patientId) {
        return ResponseEntity.ok(prescriptionService.getByPatient(patientId));
    }

    @PostMapping("/api/prescriptions/{id}/medications/{medicationId}")
    @ResponseBody
    public ResponseEntity<MedicationResponseDto> addMedication(
            @PathVariable Long id,
            @PathVariable Long medicationId) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(medicationService.addToPrescription(id, medicationId));
    }

    @DeleteMapping("/api/prescriptions/{id}/medications/{medicationId}")
    @ResponseBody
    public ResponseEntity<Void> removeMedication(
            @PathVariable Long id,
            @PathVariable Long medicationId) {
        medicationService.removeFromPrescription(id, medicationId);
        return ResponseEntity.noContent().build();
    }

    // =========================================================
    // New: Thymeleaf pages for Feature 7 (Prescription Management)
    // =========================================================

    // GET /prescriptions?patientId=&doctorId=&from=&to=&page=&size=&sort=
    @GetMapping("/prescriptions")
    public String list(@RequestParam(required = false) Long patientId,
                        @RequestParam(required = false) Long doctorId,
                        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
                        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
                        @PageableDefault(size = 10, sort = "issueDate") Pageable pageable,
                        Model model) {

        Page<PrescriptionResponseDto> prescriptions = prescriptionService.getAll(patientId, doctorId, from, to, pageable);
        model.addAttribute("prescriptions", prescriptions);
        return "prescriptions/list";
    }

    // GET /prescriptions/{id}
    @GetMapping("/prescriptions/{id}")
    public String view(@PathVariable Long id, Model model) {
        model.addAttribute("prescription", prescriptionService.getById(id));
        return "prescriptions/view";
    }

    // GET /prescriptions/new?patientId=&doctorId=&appointmentId=  (all optional)
    @GetMapping("/prescriptions/new")
    public String newForm(@RequestParam(required = false) Long patientId,
                           @RequestParam(required = false) Long doctorId,
                           @RequestParam(required = false) Long appointmentId,
                           Model model) {
        model.addAttribute("selectedPatientId", patientId);
        model.addAttribute("selectedDoctorId", doctorId);
        model.addAttribute("selectedAppointmentId", appointmentId);
        model.addAttribute("patientAppointments", loadPatientAppointments(patientId));
        return "prescriptions/form";
    }

    // POST /prescriptions  (view-form equivalent of POST /api/prescriptions/for-patient/{patientId}/by-doctor/{doctorId})
    @PostMapping("/prescriptions")
    public String create(@RequestParam Long patientId,
                          @RequestParam Long doctorId,
                          @RequestParam(required = false) Long appointmentId,
                          @RequestParam(required = false) String notes,
                          @RequestParam(required = false) List<Long> medicationIds,
                          Model model,
                          RedirectAttributes redirectAttributes) {
        try {
            PrescriptionResponseDto created = prescriptionService.create(
                    patientId, doctorId, new PrescriptionRequestDto(appointmentId, notes, medicationIds));
            redirectAttributes.addFlashAttribute("successMessage", "Prescription issued.");
            return "redirect:/prescriptions/" + created.id();
        } catch (RuntimeException ex) {
            model.addAttribute("errorMessage", "Could not issue prescription: " + ex.getMessage());
            model.addAttribute("selectedPatientId", patientId);
            model.addAttribute("selectedDoctorId", doctorId);
            model.addAttribute("selectedAppointmentId", appointmentId);
            model.addAttribute("patientAppointments", loadPatientAppointments(patientId));
            return "prescriptions/form";
        }
    }

    // GET /prescriptions/{id}/edit
    @GetMapping("/prescriptions/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        PrescriptionResponseDto prescription = prescriptionService.getById(id);
        model.addAttribute("prescription", prescription);
        // Precomputed here (rather than in the template) since Thymeleaf's
        // expression language doesn't support Java lambdas like stream().anyMatch(...).
        model.addAttribute("selectedMedicationIds",
                prescription.medications().stream().map(m -> m.id()).toList());
        return "prescriptions/edit";
    }

    // POST /prescriptions/{id}/update  (view-form equivalent of PUT /api/prescriptions/{id})
    @PostMapping("/prescriptions/{id}/update")
    public String updateFromForm(@PathVariable Long id,
                                  @RequestParam(required = false) String notes,
                                  @RequestParam(required = false) List<Long> medicationIds,
                                  RedirectAttributes redirectAttributes) {
        prescriptionService.update(id, new PrescriptionUpdateDto(notes, medicationIds));
        redirectAttributes.addFlashAttribute("successMessage", "Prescription updated.");
        return "redirect:/prescriptions/" + id;
    }

    // POST /prescriptions/{id}/delete  (view-form equivalent of DELETE /api/prescriptions/{id})
    @PostMapping("/prescriptions/{id}/delete")
    public String deleteFromForm(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        prescriptionService.delete(id);
        redirectAttributes.addFlashAttribute("successMessage", "Prescription deleted.");
        return "redirect:/prescriptions";
    }

    // POST /prescriptions/{id}/medications  (view-form equivalent of POST /api/prescriptions/{id}/medications/{medicationId})
    @PostMapping("/prescriptions/{id}/medications")
    public String addMedicationFromForm(@PathVariable Long id,
                                         @RequestParam Long medicationId,
                                         RedirectAttributes redirectAttributes) {
        medicationService.addToPrescription(id, medicationId);
        redirectAttributes.addFlashAttribute("successMessage", "Medication added.");
        return "redirect:/prescriptions/" + id;
    }

    // POST /prescriptions/{id}/medications/{medicationId}/delete  (view-form equivalent of DELETE .../medications/{medicationId})
    @PostMapping("/prescriptions/{id}/medications/{medicationId}/delete")
    public String removeMedicationFromForm(@PathVariable Long id,
                                            @PathVariable Long medicationId,
                                            RedirectAttributes redirectAttributes) {
        medicationService.removeFromPrescription(id, medicationId);
        redirectAttributes.addFlashAttribute("successMessage", "Medication removed.");
        return "redirect:/prescriptions/" + id;
    }

    private List<AppointmentResponseDto> loadPatientAppointments(Long patientId) {
        if (patientId == null) return List.of();
        return appointmentService.getByPatient(patientId);
    }
}
