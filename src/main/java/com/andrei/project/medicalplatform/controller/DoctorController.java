package com.andrei.project.medicalplatform.controller;

import com.andrei.project.medicalplatform.dto.common.UserRoleOptions;
import com.andrei.project.medicalplatform.dto.doctor.DoctorRequestDto;
import com.andrei.project.medicalplatform.dto.doctor.DoctorResponseDto;
import com.andrei.project.medicalplatform.dto.medicalunit.MedicalUnitResponseDto;
import com.andrei.project.medicalplatform.model.Specialization;
import com.andrei.project.medicalplatform.service.AppointmentService;
import com.andrei.project.medicalplatform.service.DoctorSearchService;
import com.andrei.project.medicalplatform.service.DoctorService;
import com.andrei.project.medicalplatform.service.MedicalUnitService;
import com.andrei.project.medicalplatform.service.PrescriptionService;
import com.andrei.project.medicalplatform.service.UserService;
import com.andrei.project.medicalplatform.web.form.DoctorFormDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Doctor controller: your original JSON API (unchanged, at /api/doctors,
 * annotated with @ResponseBody instead of class-level @RestController so it
 * can sit alongside the new HTML view endpoints below) plus the Thymeleaf
 * pages for Feature 2 (Doctor Management).
 *
 * Because it now mixes JSON and HTML responses, the class is @Controller and
 * every original method got an explicit @ResponseBody + its full "/api/doctors..."
 * path (method-level @RequestMapping paths do NOT escape a class-level prefix
 * in Spring MVC, so the old class-level @RequestMapping("/api/doctors") had to
 * be removed and each path made absolute instead - request-for-request the
 * API is identical to what you had).
 *
 * TODO: I don't have your MedicalUnitService.findManagers()-equivalent for
 * users, so "eligibleUsers" below is a placeholder - point it at however you
 * fetch users that can become a doctor (e.g. users with role DOCTOR not yet
 * linked to a Doctor row).
 */
@Controller
@RequiredArgsConstructor
public class DoctorController {

    private final DoctorService doctorService;
    private final DoctorSearchService doctorSearchService;
    private final MedicalUnitService medicalUnitService;
    private final AppointmentService appointmentService; // Feature 4 - shown on the doctor detail page
    private final PrescriptionService prescriptionService; // Feature 7 - shown on the doctor detail page
    private final UserService userService;

    // =========================================================
    // Existing JSON API - unchanged behavior, made explicit since
    // the class is no longer a @RestController.
    // =========================================================

    @PutMapping("/api/doctors/{id}")
    @ResponseBody
    public ResponseEntity<DoctorResponseDto> update(
            @PathVariable Long id,
            @Valid @RequestBody DoctorRequestDto request) {
        return ResponseEntity.ok(doctorService.update(id, request));
    }

    @DeleteMapping("/api/doctors/{id}")
    @ResponseBody
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        doctorService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/api/doctors/{id}")
    @ResponseBody
    public ResponseEntity<DoctorResponseDto> getById(@PathVariable Long id) {
        return ResponseEntity.ok(doctorService.getById(id));
    }

    @GetMapping("/api/doctors")
    @ResponseBody
    public ResponseEntity<Page<DoctorResponseDto>> getAll(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Specialization specialization,
            @RequestParam(required = false) Long medicalUnitId,
            @PageableDefault(size = 20, sort = "id") Pageable pageable) {
        return ResponseEntity.ok(doctorService.getAll(name, specialization, medicalUnitId, pageable));
    }

    @PostMapping("/api/doctors/{id}/specializations/{specId}")
    @ResponseBody
    public ResponseEntity<DoctorResponseDto> addSpecialization(
            @PathVariable Long id, @PathVariable Specialization specId) {
        return ResponseEntity.ok(doctorService.addSpecialization(id, specId));
    }

    @DeleteMapping("/api/doctors/{id}/specializations/{specId}")
    @ResponseBody
    public ResponseEntity<DoctorResponseDto> removeSpecialization(
            @PathVariable Long id, @PathVariable Specialization specId) {
        return ResponseEntity.ok(doctorService.removeSpecialization(id, specId));
    }

    @GetMapping("/api/doctors/search")
    @ResponseBody
    public ResponseEntity<Page<DoctorResponseDto>> search(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Specialization specialization,
            @RequestParam(required = false) Long medicalUnitId,
            @RequestParam(required = false) Boolean available,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
            @PageableDefault(size = 20, sort = "id") Pageable pageable) {
        return ResponseEntity.ok(
                doctorSearchService.search(name, specialization, medicalUnitId, available, from, to, pageable));
    }

    // =========================================================
    // New: Thymeleaf pages for Feature 2 (Doctor Management)
    // =========================================================

    @ModelAttribute("allSpecializations")
    public List<Specialization> allSpecializations() {
        return List.of(Specialization.values());
    }

    @ModelAttribute("allMedicalUnits")
    public List<MedicalUnitResponseDto> allMedicalUnits() {
        // Used only to populate the "Medical Unit" filter dropdown on the doctors list page.
        return medicalUnitService.getAll(null, null, PageRequest.of(0, 500)).getContent();
    }

    // GET /doctors?name=&specialization=&medicalUnitId=&available=&page=&size=&sort=
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

    // GET /doctors/{id}
    @GetMapping("/doctors/{id}")
    public String view(@PathVariable Long id, Model model) {
        model.addAttribute("doctor", doctorService.getById(id));
        model.addAttribute("appointments", appointmentService.getByDoctor(id)); // Feature 4
        model.addAttribute("prescriptions", // Feature 7 - reuses getAll with a doctorId filter since there's no getByDoctor for prescriptions
                prescriptionService.getAll(null, id, null, null, PageRequest.of(0, 10)));
        return "doctors/view";
    }

    // GET /medical-units/{unitId}/doctors/new
    @GetMapping("/medical-units/{unitId}/doctors/new")
    public String newForm(@PathVariable Long unitId, Model model) {
        MedicalUnitResponseDto unit = medicalUnitService.getById(unitId);

        DoctorFormDto form = new DoctorFormDto();
        form.setMedicalUnitId(unitId);

        model.addAttribute("medicalUnit", unit);
        model.addAttribute("doctor", form);
        model.addAttribute("eligibleUsers", loadEligibleUserOptions());
        return "doctors/form";
    }

    // POST /medical-units/{unitId}/doctors  -> calls the same doctorService.create
    // that the JSON API's MedicalUnitController.addDoctor uses.
    @PostMapping("/medical-units/{unitId}/doctors")
    public String create(@PathVariable Long unitId,
                          @Valid @ModelAttribute("doctor") DoctorFormDto form,
                          BindingResult result,
                          Model model,
                          RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            model.addAttribute("medicalUnit", medicalUnitService.getById(unitId));
            model.addAttribute("eligibleUsers", loadEligibleUserOptions());
            return "doctors/form";
        }

        DoctorRequestDto dto = new DoctorRequestDto(form.getUserId(), form.getLicenseNumber(), form.getSpecializations());
        doctorService.create(unitId, dto);

        redirectAttributes.addFlashAttribute("successMessage", "Doctor added.");
        return "redirect:/medical-units/" + unitId;
    }

    // GET /doctors/{id}/edit
    @GetMapping("/doctors/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        DoctorResponseDto existing = doctorService.getById(id);

        DoctorFormDto form = new DoctorFormDto();
        form.setId(existing.id());
        form.setUserId(existing.userId());
        form.setLicenseNumber(existing.licenseNumber());
        form.setSpecializations(existing.specializations());

        model.addAttribute("medicalUnit", medicalUnitService.getById(existing.medicalUnitId()));
        model.addAttribute("doctor", form);
        model.addAttribute("eligibleUsers", loadEligibleUserOptions());
        model.addAttribute("currentUser", userService.getById(existing.userId()));
        return "doctors/form";
    }

    // POST /doctors/{id}/update  (view-form equivalent of PUT /api/doctors/{id})
    @PostMapping("/doctors/{id}/update")
    public String updateFromForm(@PathVariable Long id,
                                  @Valid @ModelAttribute("doctor") DoctorFormDto form,
                                  BindingResult result,
                                  Model model,
                                  RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            form.setId(id);
            model.addAttribute("eligibleUsers", loadEligibleUserOptions());
            if (form.getUserId() != null) {
                model.addAttribute("currentUser", userService.getById(form.getUserId()));
            }
            return "doctors/form";
        }

        DoctorRequestDto dto = new DoctorRequestDto(form.getUserId(), form.getLicenseNumber(), form.getSpecializations());
        doctorService.update(id, dto);

        redirectAttributes.addFlashAttribute("successMessage", "Doctor updated.");
        return "redirect:/doctors/" + id;
    }

    // POST /doctors/{id}/delete  (view-form equivalent of DELETE /api/doctors/{id})
    @PostMapping("/doctors/{id}/delete")
    public String deleteFromForm(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        doctorService.delete(id);
        redirectAttributes.addFlashAttribute("successMessage", "Doctor removed.");
        return "redirect:/doctors";
    }

    // POST /doctors/{id}/specializations  (view-form equivalent of POST /api/doctors/{id}/specializations/{specId})
    @PostMapping("/doctors/{id}/specializations")
    public String addSpecializationFromForm(@PathVariable Long id,
                                             @RequestParam("specializationId") Specialization specializationId,
                                             RedirectAttributes redirectAttributes) {
        doctorService.addSpecialization(id, specializationId);
        redirectAttributes.addFlashAttribute("successMessage", "Specialization added.");
        return "redirect:/doctors/" + id;
    }

    // POST /doctors/{id}/specializations/{specId}/delete  (view-form equivalent of DELETE /api/doctors/{id}/specializations/{specId})
    @PostMapping("/doctors/{id}/specializations/{specId}/delete")
    public String removeSpecializationFromForm(@PathVariable Long id,
                                                @PathVariable Specialization specId,
                                                RedirectAttributes redirectAttributes) {
        doctorService.removeSpecialization(id, specId);
        redirectAttributes.addFlashAttribute("successMessage", "Specialization removed.");
        return "redirect:/doctors/" + id;
    }

    private UserRoleOptions loadEligibleUserOptions() {
        return userService.findEligibleDoctorUsers();
    }
}
