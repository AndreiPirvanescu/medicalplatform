package com.andrei.project.medicalplatform.controller;

import com.andrei.project.medicalplatform.dto.appointment.AppointmentRequestDto;
import com.andrei.project.medicalplatform.dto.appointment.AppointmentResponseDto;
import com.andrei.project.medicalplatform.dto.appointment.AppointmentUpdateDto;
import com.andrei.project.medicalplatform.dto.schedule.ScheduleResponseDto;
import com.andrei.project.medicalplatform.model.AppointmentStatus;
import com.andrei.project.medicalplatform.service.AppointmentService;
import com.andrei.project.medicalplatform.service.DoctorService;
import com.andrei.project.medicalplatform.service.PatientService;
import com.andrei.project.medicalplatform.service.ScheduleService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
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
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Appointment controller: your original JSON API (unchanged, at
 * /api/appointments, now with explicit @ResponseBody + absolute paths since
 * the class dropped its @RestController class-level mapping - same pattern
 * as MedicalUnitController/DoctorController/PatientController) plus the
 * Thymeleaf pages for Feature 4 (Appointment Management).
 *
 * Booking needs a patient AND a doctor, so PatientService/DoctorService are
 * injected here too, purely to populate the two <select> dropdowns on the
 * booking form - they were already built for Features 2/3.
 *
 * ScheduleService is injected only to show a doctor's upcoming available
 * slots on the booking form as a convenience (see Feature 5's
 * ScheduleController, which was written from scratch since no
 * ScheduleController was provided). If you don't want that coupling, delete
 * the "availableSlots" bits below and in appointments/form.html.
 */
@Controller
@Validated // needed for @NotNull on the @RequestParam method params below to actually be enforced
@RequiredArgsConstructor
public class AppointmentController {

    private final AppointmentService appointmentService;
    private final PatientService patientService;
    private final DoctorService doctorService;
    private final ScheduleService scheduleService;

    @ModelAttribute("allStatuses")
    public List<AppointmentStatus> allStatuses() {
        return List.of(AppointmentStatus.values());
    }

    @ModelAttribute("allPatients")
    public List<com.andrei.project.medicalplatform.dto.patient.PatientResponseDto> allPatients() {
        // used only to populate filter/booking dropdowns
        return patientService.getAll(null, null, PageRequest.of(0, 500)).getContent();
    }

    @ModelAttribute("allDoctors")
    public List<com.andrei.project.medicalplatform.dto.doctor.DoctorResponseDto> allDoctors() {
        // used only to populate filter/booking dropdowns
        return doctorService.getAll(null, null, null, PageRequest.of(0, 500)).getContent();
    }

    // =========================================================
    // Existing JSON API - unchanged behavior, made explicit.
    // =========================================================

    @PostMapping("/api/appointments/for-patient/{patientId}/with-doctor/{doctorId}")
    @ResponseBody
    public ResponseEntity<AppointmentResponseDto> book(
            @PathVariable Long patientId,
            @PathVariable Long doctorId,
            @Valid @RequestBody AppointmentRequestDto request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(appointmentService.create(patientId, doctorId, request));
    }

    @PutMapping("/api/appointments/{id}")
    @ResponseBody
    public ResponseEntity<AppointmentResponseDto> update(
            @PathVariable Long id,
            @Valid @RequestBody AppointmentUpdateDto request) {
        return ResponseEntity.ok(appointmentService.update(id, request));
    }

    @DeleteMapping("/api/appointments/{id}")
    @ResponseBody
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        appointmentService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/api/appointments/{id}")
    @ResponseBody
    public ResponseEntity<AppointmentResponseDto> getById(@PathVariable Long id) {
        return ResponseEntity.ok(appointmentService.getById(id));
    }

    @GetMapping("/api/appointments")
    @ResponseBody
    public ResponseEntity<Page<AppointmentResponseDto>> getAll(
            @RequestParam(required = false) Long patientId,
            @RequestParam(required = false) Long doctorId,
            @RequestParam(required = false) AppointmentStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
            @PageableDefault(size = 20, sort = "appointmentDateTime") Pageable pageable) {
        return ResponseEntity.ok(appointmentService.getAll(patientId, doctorId, status, from, to, pageable));
    }

    @GetMapping("/api/appointments/by-doctor/{doctorId}")
    @ResponseBody
    public ResponseEntity<List<AppointmentResponseDto>> getByDoctor(@PathVariable Long doctorId) {
        return ResponseEntity.ok(appointmentService.getByDoctor(doctorId));
    }

    @GetMapping("/api/appointments/by-patient/{patientId}")
    @ResponseBody
    public ResponseEntity<List<AppointmentResponseDto>> getByPatient(@PathVariable Long patientId) {
        return ResponseEntity.ok(appointmentService.getByPatient(patientId));
    }

    // =========================================================
    // New: Thymeleaf pages for Feature 4 (Appointment Management)
    // =========================================================

    // GET /appointments?patientId=&doctorId=&status=&from=&to=&page=&size=&sort=
    @GetMapping("/appointments")
    public String list(@RequestParam(required = false) Long patientId,
                        @RequestParam(required = false) Long doctorId,
                        @RequestParam(required = false) AppointmentStatus status,
                        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
                        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
                        @PageableDefault(size = 10, sort = "appointmentDateTime") Pageable pageable,
                        Model model) {

        Page<AppointmentResponseDto> appointments =
                appointmentService.getAll(patientId, doctorId, status, from, to, pageable);
        model.addAttribute("appointments", appointments);
        return "appointments/list";
    }

    // GET /appointments/{id}
    @GetMapping("/appointments/{id}")
    public String view(@PathVariable Long id, Model model) {
        model.addAttribute("appointment", appointmentService.getById(id));
        return "appointments/view";
    }

    // GET /appointments/new?patientId=&doctorId=  (both optional - lets the
    // booking form be reached from a blank state, from a patient's page with
    // patientId prefilled, or from a doctor's page with doctorId prefilled)
    @GetMapping("/appointments/new")
    public String newForm(@RequestParam(required = false) Long patientId,
                           @RequestParam(required = false) Long doctorId,
                           Model model) {
        model.addAttribute("selectedPatientId", patientId);
        model.addAttribute("selectedDoctorId", doctorId);
        model.addAttribute("availableSlots", loadAvailableSlots(doctorId));
        return "appointments/form";
    }

    // POST /appointments  (view-form equivalent of POST /api/appointments/for-patient/{patientId}/with-doctor/{doctorId})
    @PostMapping("/appointments")
    public String create(@RequestParam @NotNull Long patientId,
                          @RequestParam @NotNull Long doctorId,
                          @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime appointmentDateTime,
                          RedirectAttributes redirectAttributes,
                          Model model) {
        try {
            appointmentService.create(patientId, doctorId, new AppointmentRequestDto(appointmentDateTime));
        } catch (RuntimeException ex) {
            // e.g. slot no longer available / overlapping appointment - adjust the
            // catch type to whatever exception your service actually throws.
            model.addAttribute("errorMessage", "Could not book the appointment: " + ex.getMessage());
            model.addAttribute("selectedPatientId", patientId);
            model.addAttribute("selectedDoctorId", doctorId);
            model.addAttribute("availableSlots", loadAvailableSlots(doctorId));
            return "appointments/form";
        }

        redirectAttributes.addFlashAttribute("successMessage", "Appointment booked.");
        return "redirect:/patients/" + patientId;
    }

    // GET /appointments/{id}/edit
    @GetMapping("/appointments/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        model.addAttribute("appointment", appointmentService.getById(id));
        return "appointments/edit";
    }

    // POST /appointments/{id}/update  (view-form equivalent of PUT /api/appointments/{id})
    @PostMapping("/appointments/{id}/update")
    public String updateFromForm(@PathVariable Long id,
                                  @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime appointmentDateTime,
                                  @RequestParam AppointmentStatus status,
                                  RedirectAttributes redirectAttributes) {
        appointmentService.update(id, new AppointmentUpdateDto(appointmentDateTime, status));
        redirectAttributes.addFlashAttribute("successMessage", "Appointment updated.");
        return "redirect:/appointments/" + id;
    }

    // POST /appointments/{id}/delete  (view-form equivalent of DELETE /api/appointments/{id})
    @PostMapping("/appointments/{id}/delete")
    public String deleteFromForm(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        appointmentService.delete(id);
        redirectAttributes.addFlashAttribute("successMessage", "Appointment cancelled.");
        return "redirect:/appointments";
    }

    private List<ScheduleResponseDto> loadAvailableSlots(Long doctorId) {
        if (doctorId == null) return List.of();
        LocalDateTime from = LocalDateTime.now();
        LocalDateTime to = from.plusDays(30);
        return scheduleService.getAvailableByDoctor(doctorId, from, to);
    }
}
