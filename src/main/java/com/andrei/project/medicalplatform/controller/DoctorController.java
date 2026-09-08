package com.andrei.project.medicalplatform.controller;

import com.andrei.project.medicalplatform.dto.common.UserOptionDto;
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
 * "eligibleUsers" (UserService.findEligibleDoctorUsers() - users with NO
 * role at all) only feeds the CREATE form's dropdown. On EDIT, the linked
 * user account can't be changed any more - the form shows it read-only via
 * "currentUser" and pins userId server-side in updateFromForm, so it never
 * gets reassigned.
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

    // GET /doctors/new?medicalUnitId=  (medicalUnitId optional - lets this be
    // reached bare from the Doctors page, or preselected from a unit's page)
    @GetMapping("/doctors/new")
    public String newForm(@RequestParam(required = false) Long medicalUnitId, Model model) {
        DoctorFormDto form = new DoctorFormDto();
        form.setMedicalUnitId(medicalUnitId);

        model.addAttribute("doctor", form);
        model.addAttribute("eligibleUsers", loadEligibleUserOptions());
        return "doctors/form";
    }

    // POST /doctors  (view-form equivalent of POST /api/medicalUnits/{id}/doctors,
    // except the unit is now picked on the form itself instead of coming from
    // the URL - calls the same doctorService.create the JSON API uses)
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

    // GET /doctors/{id}/edit
    @GetMapping("/doctors/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        DoctorResponseDto existing = doctorService.getById(id);

        DoctorFormDto form = new DoctorFormDto();
        form.setId(existing.id());
        form.setMedicalUnitId(existing.medicalUnitId()); // not reassignable - see doctors/form.html; only kept so the required-field validation doesn't trip on edit
        form.setUserId(existing.userId());
        form.setLicenseNumber(existing.licenseNumber());
        form.setSpecializations(existing.specializations());

        model.addAttribute("medicalUnit", medicalUnitService.getById(existing.medicalUnitId()));
        model.addAttribute("doctor", form);
        // No "eligibleUsers" here on purpose - the linked user account can't be
        // changed once a doctor profile exists, so the edit form only shows
        // "currentUser" read-only (see doctors/form.html).
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
        // The linked user account is not editable from this form (its field is
        // a disabled/hidden input) - pin it to the existing value server-side
        // too, so the doctor profile can never be reassigned to a different
        // user even if the hidden field were tampered with.
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

    // POST /doctors/{id}/delete  (view-form equivalent of DELETE /api/doctors/{id})
    @PostMapping("/doctors/{id}/delete")
    public String deleteFromForm(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            doctorService.delete(id);
            redirectAttributes.addFlashAttribute("successMessage", "Doctor removed.");
            return "redirect:/doctors";
        } catch (RuntimeException ex) {
            // This is why "delete" could look like it does nothing: doctorService.delete
            // is a plain doctorRepository.delete(...) with no guard, so if this doctor
            // still has appointments/prescriptions/medical records pointing at them (and
            // there's no cascade delete configured), the database rejects it with a
            // DataIntegrityViolationException that was going unhandled - Spring showed
            // its default error page instead of returning to the list, and the doctor
            // was never actually removed. Surface the real reason instead.
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Could not remove doctor: " + ex.getMessage()
                            + ". They likely still have appointments, prescriptions or records linked to them - remove those first.");
            return "redirect:/doctors/" + id;
        }
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

    private List<UserOptionDto> loadEligibleUserOptions() {
        return userService.findEligibleDoctorUsers();
    }
}
