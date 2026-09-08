package com.andrei.project.medicalplatform.controller;

import com.andrei.project.medicalplatform.dto.common.UserOptionDto;
import com.andrei.project.medicalplatform.dto.patient.PatientRequestDto;
import com.andrei.project.medicalplatform.dto.patient.PatientResponseDto;
import com.andrei.project.medicalplatform.service.AppointmentService;
import com.andrei.project.medicalplatform.service.PatientService;
import com.andrei.project.medicalplatform.service.PrescriptionService;
import com.andrei.project.medicalplatform.service.UserService;
import com.andrei.project.medicalplatform.web.form.PatientFormDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class PatientController {

    private final PatientService patientService;
    private final AppointmentService appointmentService;
    private final PrescriptionService prescriptionService;
    private final UserService userService;

    @GetMapping("/patients")
    public String list(@RequestParam(required = false) String firstName,
                        @RequestParam(required = false) String lastName,
                        @PageableDefault(size = 10, sort = "id") Pageable pageable,
                        Model model) {

        Page<PatientResponseDto> patients = patientService.getAll(firstName, lastName, pageable);
        model.addAttribute("patients", patients);
        return "patients/list";
    }

    @GetMapping("/patients/{id}")
    public String view(@PathVariable Long id, Model model) {
        model.addAttribute("patient", patientService.getById(id));
        model.addAttribute("appointments", appointmentService.getByPatient(id));
        model.addAttribute("prescriptions", prescriptionService.getByPatient(id));
        return "patients/view";
    }

    @GetMapping("/patients/new")
    public String newForm(Model model) {
        model.addAttribute("patient", new PatientFormDto());
        model.addAttribute("eligibleUsers", loadEligibleUserOptions());
        return "patients/form";
    }

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

    @GetMapping("/patients/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        PatientResponseDto existing = patientService.getById(id);

        PatientFormDto form = new PatientFormDto();
        form.setId(existing.id());
        form.setUserId(existing.userId());
        form.setDateOfBirth(existing.dateOfBirth());
        form.setBloodType(existing.bloodType());

        model.addAttribute("patient", form);
        model.addAttribute("currentUser", userService.getById(existing.userId()));
        return "patients/form";
    }

    @PostMapping("/patients/{id}/update")
    public String updateFromForm(@PathVariable Long id,
                                  @Valid @ModelAttribute("patient") PatientFormDto form,
                                  BindingResult result,
                                  Model model,
                                  RedirectAttributes redirectAttributes) {
        Long existingUserId = patientService.getById(id).userId();
        form.setUserId(existingUserId);

        if (result.hasErrors()) {
            form.setId(id);
            model.addAttribute("currentUser", userService.getById(existingUserId));
            return "patients/form";
        }

        PatientRequestDto dto = new PatientRequestDto(existingUserId, form.getDateOfBirth(), form.getBloodType());
        patientService.update(id, dto);

        redirectAttributes.addFlashAttribute("successMessage", "Patient updated.");
        return "redirect:/patients/" + id;
    }

    @PostMapping("/patients/{id}/delete")
    public String deleteFromForm(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            patientService.delete(id);
            redirectAttributes.addFlashAttribute("successMessage", "Patient deleted.");
            return "redirect:/patients";
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Could not delete patient: " + ex.getMessage()
                            + ". They likely still have appointments, prescriptions or records linked to them - remove those first.");
            return "redirect:/patients/" + id;
        }
    }

    private List<UserOptionDto> loadEligibleUserOptions() {
        return userService.findEligiblePatientUsers();
    }
}
