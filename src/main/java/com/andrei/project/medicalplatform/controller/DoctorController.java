package com.andrei.project.medicalplatform.controller;

import com.andrei.project.medicalplatform.dto.common.UserOptionDto;
import com.andrei.project.medicalplatform.dto.doctor.DoctorRequestDto;
import com.andrei.project.medicalplatform.dto.doctor.DoctorResponseDto;
import com.andrei.project.medicalplatform.dto.medicalunit.MedicalUnitResponseDto;
import com.andrei.project.medicalplatform.model.Specialization;
import com.andrei.project.medicalplatform.service.*;
import com.andrei.project.medicalplatform.web.form.DoctorFormDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
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
public class DoctorController {

    private final DoctorService doctorService;
    private final DoctorSearchService doctorSearchService;
    private final MedicalUnitService medicalUnitService;
    private final AppointmentService appointmentService;
    private final PrescriptionService prescriptionService;
    private final UserService userService;


    @ModelAttribute("allSpecializations")
    public List<Specialization> allSpecializations() {
        return List.of(Specialization.values());
    }

    @ModelAttribute("allMedicalUnits")
    public List<MedicalUnitResponseDto> allMedicalUnits() {
        return medicalUnitService.getAll(null, null, PageRequest.of(0, 500)).getContent();
    }

    @GetMapping("/doctors")
    public String list(@RequestParam(required = false) String name,
                        @RequestParam(required = false) Specialization specialization,
                        @RequestParam(required = false) Long medicalUnitId,
                        @RequestParam(required = false) Boolean available,
                        @PageableDefault(size = 10, sort = "id") Pageable pageable,
                        Model model) {

        Page<DoctorResponseDto> doctors = (available != null && available)
                ? doctorSearchService.search(name, specialization, medicalUnitId, true, null, null, pageable)
                : doctorService.getAll(name, specialization, medicalUnitId, pageable);

        model.addAttribute("doctors", doctors);
        return "doctors/list";
    }

    @GetMapping("/doctors/{id}")
    public String view(@PathVariable Long id, Model model) {
        model.addAttribute("doctor", doctorService.getById(id));
        model.addAttribute("appointments", appointmentService.getByDoctor(id)); // Feature 4
        model.addAttribute("prescriptions", // Feature 7 - reuses getAll with a doctorId filter since there's no getByDoctor for prescriptions
                prescriptionService.getAll(null, id, null, null, PageRequest.of(0, 10)));
        return "doctors/view";
    }

    @GetMapping("/doctors/new")
    public String newForm(@RequestParam(required = false) Long medicalUnitId, Model model) {
        DoctorFormDto form = new DoctorFormDto();
        form.setMedicalUnitId(medicalUnitId);

        model.addAttribute("doctor", form);
        model.addAttribute("eligibleUsers", loadEligibleUserOptions());
        return "doctors/form";
    }

    @PostMapping("/doctors")
    public String create(@Valid @ModelAttribute("doctor") DoctorFormDto form,
                          BindingResult result,
                          Model model,
                          RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            model.addAttribute("eligibleUsers", loadEligibleUserOptions());
            return "doctors/form";
        }

        DoctorRequestDto dto = new DoctorRequestDto(form.getUserId(), form.getLicenseNumber(), form.getSpecializations());
        DoctorResponseDto created = doctorService.create(form.getMedicalUnitId(), dto);

        redirectAttributes.addFlashAttribute("successMessage", "Doctor added.");
        return "redirect:/doctors/" + created.id();
    }

    @GetMapping("/doctors/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        DoctorResponseDto existing = doctorService.getById(id);

        DoctorFormDto form = new DoctorFormDto();
        form.setId(existing.id());
        form.setMedicalUnitId(existing.medicalUnitId());
        form.setUserId(existing.userId());
        form.setLicenseNumber(existing.licenseNumber());
        form.setSpecializations(existing.specializations());

        model.addAttribute("medicalUnit", medicalUnitService.getById(existing.medicalUnitId()));
        model.addAttribute("doctor", form);
        model.addAttribute("currentUser", userService.getById(existing.userId()));
        return "doctors/form";
    }

    @PostMapping("/doctors/{id}/update")
    public String updateFromForm(@PathVariable Long id,
                                  @Valid @ModelAttribute("doctor") DoctorFormDto form,
                                  BindingResult result,
                                  Model model,
                                  RedirectAttributes redirectAttributes) {
        Long existingUserId = doctorService.getById(id).userId();
        form.setUserId(existingUserId);

        if (result.hasErrors()) {
            form.setId(id);
            model.addAttribute("currentUser", userService.getById(existingUserId));
            return "doctors/form";
        }

        DoctorRequestDto dto = new DoctorRequestDto(existingUserId, form.getLicenseNumber(), form.getSpecializations());
        doctorService.update(id, dto);

        redirectAttributes.addFlashAttribute("successMessage", "Doctor updated.");
        return "redirect:/doctors/" + id;
    }

    @PostMapping("/doctors/{id}/delete")
    public String deleteFromForm(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        doctorService.delete(id);
        redirectAttributes.addFlashAttribute("successMessage", "Doctor removed.");
        return "redirect:/doctors";
    }

    @PostMapping("/doctors/{id}/specializations")
    public String addSpecializationFromForm(@PathVariable Long id,
                                             @RequestParam("specializationId") Specialization specializationId,
                                             RedirectAttributes redirectAttributes) {
        doctorService.addSpecialization(id, specializationId);
        redirectAttributes.addFlashAttribute("successMessage", "Specialization added.");
        return "redirect:/doctors/" + id;
    }

    @PostMapping("/doctors/{id}/specializations/{specId}/delete")
    public String removeSpecializationFromForm(@PathVariable Long id,
                                                @PathVariable Specialization specId,
                                                RedirectAttributes redirectAttributes) {
        doctorService.removeSpecialization(id, specId);
        redirectAttributes.addFlashAttribute("successMessage", "Specialization removed.");
        return "redirect:/doctors/" + id;
    }

    private List<UserOptionDto> loadEligibleUserOptions() {
        return userService.findEligibleDoctorUsers();
    }
}
