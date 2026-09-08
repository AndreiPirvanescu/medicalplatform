package com.andrei.project.medicalplatform.controller;

import com.andrei.project.medicalplatform.dto.doctor.DoctorResponseDto;
import com.andrei.project.medicalplatform.dto.medicalunit.MedicalUnitRequestDto;
import com.andrei.project.medicalplatform.dto.medicalunit.MedicalUnitResponseDto;
import com.andrei.project.medicalplatform.service.DoctorService;
import com.andrei.project.medicalplatform.service.MedicalUnitService;
import com.andrei.project.medicalplatform.service.UserService;
import com.andrei.project.medicalplatform.web.form.MedicalUnitFormDto;
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
public class MedicalUnitController {

    private final MedicalUnitService medicalUnitService;
    private final DoctorService doctorService;
    private final UserService userService;

    @GetMapping("/medical-units")
    public String list(@RequestParam(required = false) String name,
                        @RequestParam(required = false) String location,
                        @PageableDefault(size = 10, sort = "name") Pageable pageable,
                        Model model) {

        Page<MedicalUnitResponseDto> medicalUnits = medicalUnitService.getAll(name, location, pageable);
        model.addAttribute("medicalUnits", medicalUnits);
        return "medical-units/list";
    }

    @GetMapping("/medical-units/{id}")
    public String view(@PathVariable Long id, Model model) {
        MedicalUnitResponseDto medicalUnit = medicalUnitService.getById(id);
        List<DoctorResponseDto> doctors = doctorService.getByMedicalUnit(id);

        model.addAttribute("medicalUnit", medicalUnit);
        model.addAttribute("doctors", doctors);
        return "medical-units/view";
    }

    @GetMapping("/medical-units/new")
    public String newForm(Model model) {
        model.addAttribute("medicalUnit", new MedicalUnitFormDto());
        model.addAttribute("managers", userService.findManagerCandidates(null));
        return "medical-units/form";
    }

    @PostMapping("/medical-units")
    public String createFromForm(@Valid @ModelAttribute("medicalUnit") MedicalUnitFormDto form,
                                  BindingResult result,
                                  Model model,
                                  RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            model.addAttribute("managers", userService.findManagerCandidates(form.getManagerId()));
            return "medical-units/form";
        }

        MedicalUnitRequestDto dto = new MedicalUnitRequestDto(
                form.getName(), form.getEmail(), form.getPhone(), form.getAddress(), form.getManagerId());
        MedicalUnitResponseDto created = medicalUnitService.create(dto);

        redirectAttributes.addFlashAttribute("successMessage", "Medical unit \"" + created.name() + "\" created.");
        return "redirect:/medical-units";
    }

    @GetMapping("/medical-units/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        MedicalUnitResponseDto existing = medicalUnitService.getById(id);

        MedicalUnitFormDto form = new MedicalUnitFormDto();
        form.setId(existing.id());
        form.setName(existing.name());
        form.setEmail(existing.email());
        form.setPhone(existing.phone());
        form.setAddress(existing.address());
        form.setManagerId(existing.managerId());

        model.addAttribute("medicalUnit", form);
        model.addAttribute("managers", userService.findManagerCandidates(existing.managerId()));
        if (existing.managerId() != null) {
            model.addAttribute("currentManager", userService.getById(existing.managerId()));
        }
        return "medical-units/form";
    }

    @PostMapping("/medical-units/{id}/update")
    public String updateFromForm(@PathVariable Long id,
                                  @Valid @ModelAttribute("medicalUnit") MedicalUnitFormDto form,
                                  BindingResult result,
                                  Model model,
                                  RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            form.setId(id);
            model.addAttribute("managers", userService.findManagerCandidates(form.getManagerId()));
            if (form.getManagerId() != null) {
                model.addAttribute("currentManager", userService.getById(form.getManagerId()));
            }
            return "medical-units/form";
        }

        MedicalUnitRequestDto dto = new MedicalUnitRequestDto(
                form.getName(), form.getEmail(), form.getPhone(), form.getAddress(), form.getManagerId());
        medicalUnitService.update(id, dto);

        redirectAttributes.addFlashAttribute("successMessage", "Medical unit updated.");
        return "redirect:/medical-units/" + id;
    }

    @PostMapping("/medical-units/{id}/delete")
    public String deleteFromForm(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            medicalUnitService.delete(id);
            redirectAttributes.addFlashAttribute("successMessage", "Medical unit deleted.");
            return "redirect:/medical-units";
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Could not delete medical unit: " + ex.getMessage()
                            + ". It likely still has doctors assigned to it - reassign or remove those first.");
            return "redirect:/medical-units/" + id;
        }
    }

}
