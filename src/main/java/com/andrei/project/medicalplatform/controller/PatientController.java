package com.andrei.project.medicalplatform.controller;

import com.andrei.project.medicalplatform.dto.common.UserOptionDto;
import com.andrei.project.medicalplatform.dto.patient.PatientRequestDto;
import com.andrei.project.medicalplatform.dto.patient.PatientResponseDto;
import com.andrei.project.medicalplatform.service.AppointmentService;
import com.andrei.project.medicalplatform.service.PatientService;
import com.andrei.project.medicalplatform.web.form.PatientFormDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

/**
 * Patient controller: your original JSON API (unchanged, at /api/patients,
 * now with explicit @ResponseBody + absolute paths since the class dropped
 * its @RestController class-level mapping) plus the Thymeleaf pages for
 * Feature 3 (Patient Management).
 *
 * TODO: "eligibleUsers" (for the userId dropdown on the register/edit form)
 * is a placeholder - point it at your real "users eligible to register as a
 * patient" query.
 */
@Controller
@RequiredArgsConstructor
public class PatientController {

    private final PatientService patientService;
    private final AppointmentService appointmentService; // Feature 4 - shown on the patient detail page
    // private final UserService userService; // TODO: wire up your real user lookup

    // =========================================================
    // Existing JSON API - unchanged behavior, made explicit.
    // =========================================================

    @PostMapping("/api/patients")
    @ResponseBody
    public ResponseEntity<PatientResponseDto> create(@Valid @RequestBody PatientRequestDto request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(patientService.create(request));
    }

    @PutMapping("/api/patients/{id}")
    @ResponseBody
    public ResponseEntity<PatientResponseDto> update(
            @PathVariable Long id,
            @Valid @RequestBody PatientRequestDto request) {
        return ResponseEntity.ok(patientService.update(id, request));
    }

    @DeleteMapping("/api/patients/{id}")
    @ResponseBody
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        patientService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/api/patients/{id}")
    @ResponseBody
    public ResponseEntity<PatientResponseDto> getById(@PathVariable Long id) {
        return ResponseEntity.ok(patientService.getById(id));
    }

    @GetMapping("/api/patients")
    @ResponseBody
    public ResponseEntity<Page<PatientResponseDto>> getAll(
            @RequestParam(required = false) String firstName,
            @RequestParam(required = false) String lastName,
            @PageableDefault(size = 20, sort = "id") Pageable pageable) {
        return ResponseEntity.ok(patientService.getAll(firstName, lastName, pageable));
    }

    // =========================================================
    // New: Thymeleaf pages for Feature 3 (Patient Management)
    // =========================================================

    // GET /patients?firstName=&lastName=&page=&size=&sort=
    @GetMapping("/patients")
    public String list(@RequestParam(required = false) String firstName,
                        @RequestParam(required = false) String lastName,
                        @PageableDefault(size = 10, sort = "id") Pageable pageable,
                        Model model) {

        Page<PatientResponseDto> patients = patientService.getAll(firstName, lastName, pageable);
        model.addAttribute("patients", patients);
        return "patients/list";
    }

    // GET /patients/{id}
    @GetMapping("/patients/{id}")
    public String view(@PathVariable Long id, Model model) {
        model.addAttribute("patient", patientService.getById(id));
        model.addAttribute("appointments", appointmentService.getByPatient(id)); // Feature 4
        return "patients/view";
    }

    // GET /patients/new
    @GetMapping("/patients/new")
    public String newForm(Model model) {
        model.addAttribute("patient", new PatientFormDto());
        model.addAttribute("eligibleUsers", loadEligibleUserOptions());
        return "patients/form";
    }

    // POST /patients  (view-form equivalent of POST /api/patients)
    @PostMapping("/patients")
    public String createFromForm(@Valid @ModelAttribute("patient") PatientFormDto form,
                                  BindingResult result,
                                  Model model,
                                  RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            model.addAttribute("eligibleUsers", loadEligibleUserOptions());
            return "patients/form";
        }

        PatientRequestDto dto = new PatientRequestDto(form.getUserId(), form.getDateOfBirth(), form.getBloodType());
        PatientResponseDto created = patientService.create(dto);

        redirectAttributes.addFlashAttribute("successMessage",
                "Patient \"" + created.firstName() + " " + created.lastName() + "\" registered.");
        return "redirect:/patients";
    }

    // GET /patients/{id}/edit
    @GetMapping("/patients/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        PatientResponseDto existing = patientService.getById(id);

        PatientFormDto form = new PatientFormDto();
        form.setId(existing.id());
        form.setUserId(existing.userId());
        form.setDateOfBirth(existing.dateOfBirth());
        form.setBloodType(existing.bloodType());

        model.addAttribute("patient", form);
        model.addAttribute("eligibleUsers", loadEligibleUserOptions());
        return "patients/form";
    }

    // POST /patients/{id}/update  (view-form equivalent of PUT /api/patients/{id})
    @PostMapping("/patients/{id}/update")
    public String updateFromForm(@PathVariable Long id,
                                  @Valid @ModelAttribute("patient") PatientFormDto form,
                                  BindingResult result,
                                  Model model,
                                  RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            form.setId(id);
            model.addAttribute("eligibleUsers", loadEligibleUserOptions());
            return "patients/form";
        }

        PatientRequestDto dto = new PatientRequestDto(form.getUserId(), form.getDateOfBirth(), form.getBloodType());
        patientService.update(id, dto);

        redirectAttributes.addFlashAttribute("successMessage", "Patient updated.");
        return "redirect:/patients/" + id;
    }

    // POST /patients/{id}/delete  (view-form equivalent of DELETE /api/patients/{id})
    @PostMapping("/patients/{id}/delete")
    public String deleteFromForm(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        patientService.delete(id);
        redirectAttributes.addFlashAttribute("successMessage", "Patient deleted.");
        return "redirect:/patients";
    }

    private List<UserOptionDto> loadEligibleUserOptions() {
        // TODO: replace with your real "users eligible to register as a patient" query
        return List.of();
    }
}
