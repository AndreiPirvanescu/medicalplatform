package com.andrei.project.medicalplatform.controller;

import com.andrei.project.medicalplatform.dto.common.UserRoleOptions;
import com.andrei.project.medicalplatform.dto.doctor.DoctorRequestDto;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

/**
 * Medical unit controller: your original JSON API (unchanged, at
 * /api/medicalUnits, now with explicit @ResponseBody + absolute paths since
 * the class dropped its @RestController class-level mapping) plus the
 * Thymeleaf pages for Feature 1 (Medical Unit Management).
 *
 * NOTE ON MedicalUnitRequestDto: you only shared MedicalUnitResponseDto with
 * me, not the request-side DTO your create/update endpoints already use. I
 * assumed it mirrors the response minus computed fields (name, email, phone,
 * address, managerId) - if your real one differs, adjust MedicalUnitFormDto
 * and the two spots below that build a MedicalUnitRequestDto from the form.
 *
 * TODO: "managers" (for the manager dropdown) is a placeholder - point it at
 * your real "users eligible to manage a unit" query.
 */
@Controller
@RequiredArgsConstructor
public class MedicalUnitController {

    private final MedicalUnitService medicalUnitService;
    private final DoctorService doctorService;
    private final UserService userService;

    // =========================================================
    // Existing JSON API - unchanged behavior, made explicit.
    // =========================================================

    @PostMapping("/api/medicalUnits")
    @ResponseBody
    public ResponseEntity<MedicalUnitResponseDto> create(@Valid @RequestBody MedicalUnitRequestDto request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(medicalUnitService.create(request));
    }

    @PutMapping("/api/medicalUnits/{id}")
    @ResponseBody
    public ResponseEntity<MedicalUnitResponseDto> update(
            @PathVariable Long id,
            @Valid @RequestBody MedicalUnitRequestDto request) {
        return ResponseEntity.ok(medicalUnitService.update(id, request));
    }

    @DeleteMapping("/api/medicalUnits/{id}")
    @ResponseBody
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        medicalUnitService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/api/medicalUnits/{id}")
    @ResponseBody
    public ResponseEntity<MedicalUnitResponseDto> getById(@PathVariable Long id) {
        return ResponseEntity.ok(medicalUnitService.getById(id));
    }

    @GetMapping("/api/medicalUnits/by-manager/{managerId}")
    @ResponseBody
    public ResponseEntity<MedicalUnitResponseDto> getByManager(@PathVariable Long managerId) {
        return ResponseEntity.ok(medicalUnitService.getByManagerId(managerId));
    }

    @GetMapping("/api/medicalUnits")
    @ResponseBody
    public ResponseEntity<Page<MedicalUnitResponseDto>> getAll(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String location,
            @PageableDefault(size = 20, sort = "name") Pageable pageable) {
        return ResponseEntity.ok(medicalUnitService.getAll(name, location, pageable));
    }

    @PostMapping("/api/medicalUnits/{id}/doctors")
    @ResponseBody
    public ResponseEntity<DoctorResponseDto> addDoctor(
            @PathVariable Long id, @Valid @RequestBody DoctorRequestDto request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(doctorService.create(id, request));
    }

    @GetMapping("/api/medicalUnits/{id}/doctors")
    @ResponseBody
    public ResponseEntity<List<DoctorResponseDto>> getDoctors(@PathVariable Long id) {
        return ResponseEntity.ok(doctorService.getByMedicalUnit(id));
    }

    // =========================================================
    // New: Thymeleaf pages for Feature 1 (Medical Unit Management)
    // =========================================================

    // GET /medical-units?name=&location=&page=&size=&sort=
    @GetMapping("/medical-units")
    public String list(@RequestParam(required = false) String name,
                        @RequestParam(required = false) String location,
                        @PageableDefault(size = 10, sort = "name") Pageable pageable,
                        Model model) {

        Page<MedicalUnitResponseDto> medicalUnits = medicalUnitService.getAll(name, location, pageable);
        model.addAttribute("medicalUnits", medicalUnits);
        return "medical-units/list";
    }

    // GET /medical-units/{id}
    @GetMapping("/medical-units/{id}")
    public String view(@PathVariable Long id, Model model) {
        MedicalUnitResponseDto medicalUnit = medicalUnitService.getById(id);
        List<DoctorResponseDto> doctors = doctorService.getByMedicalUnit(id);

        model.addAttribute("medicalUnit", medicalUnit);
        model.addAttribute("doctors", doctors);
        return "medical-units/view";
    }

    // GET /medical-units/new
    @GetMapping("/medical-units/new")
    public String newForm(Model model) {
        model.addAttribute("medicalUnit", new MedicalUnitFormDto());
        model.addAttribute("managers", loadManagerOptions());
        return "medical-units/form";
    }

    // POST /medical-units  (view-form equivalent of POST /api/medicalUnits)
    @PostMapping("/medical-units")
    public String createFromForm(@Valid @ModelAttribute("medicalUnit") MedicalUnitFormDto form,
                                  BindingResult result,
                                  Model model,
                                  RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            model.addAttribute("managers", loadManagerOptions());
            return "medical-units/form";
        }

        MedicalUnitRequestDto dto = new MedicalUnitRequestDto(
                form.getName(), form.getEmail(), form.getPhone(), form.getAddress(), form.getManagerId());
        MedicalUnitResponseDto created = medicalUnitService.create(dto);

        redirectAttributes.addFlashAttribute("successMessage", "Medical unit \"" + created.name() + "\" created.");
        return "redirect:/medical-units";
    }

    // GET /medical-units/{id}/edit
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
        model.addAttribute("managers", loadManagerOptions());
        if (existing.managerId() != null) {
            model.addAttribute("currentManager", userService.getById(existing.managerId()));
        }
        return "medical-units/form";
    }

    // POST /medical-units/{id}/update  (view-form equivalent of PUT /api/medicalUnits/{id})
    @PostMapping("/medical-units/{id}/update")
    public String updateFromForm(@PathVariable Long id,
                                  @Valid @ModelAttribute("medicalUnit") MedicalUnitFormDto form,
                                  BindingResult result,
                                  Model model,
                                  RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            form.setId(id);
            model.addAttribute("managers", loadManagerOptions());
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

    // POST /medical-units/{id}/delete  (view-form equivalent of DELETE /api/medicalUnits/{id})
    @PostMapping("/medical-units/{id}/delete")
    public String deleteFromForm(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        medicalUnitService.delete(id);
        redirectAttributes.addFlashAttribute("successMessage", "Medical unit deleted.");
        return "redirect:/medical-units";
    }

    private UserRoleOptions loadManagerOptions() {
        return userService.findManagers();
    }
}
