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
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.List;

@Controller
@Validated
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
        return patientService.getAll(null, null, PageRequest.of(0, 500)).getContent();
    }

    @ModelAttribute("allDoctors")
    public List<com.andrei.project.medicalplatform.dto.doctor.DoctorResponseDto> allDoctors() {
        return doctorService.getAll(null, null, null, PageRequest.of(0, 500)).getContent();
    }

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

    @GetMapping("/appointments/{id}")
    public String view(@PathVariable Long id, Model model) {
        model.addAttribute("appointment", appointmentService.getById(id));
        return "appointments/view";
    }

    @GetMapping("/appointments/new")
    public String newForm(@RequestParam(required = false) Long patientId,
                           @RequestParam(required = false) Long doctorId,
                           Model model) {
        model.addAttribute("selectedPatientId", patientId);
        model.addAttribute("selectedDoctorId", doctorId);
        model.addAttribute("availableSlots", loadAvailableSlots(doctorId));
        return "appointments/form";
    }

    @PostMapping("/appointments")
    public String create(@RequestParam @NotNull Long patientId,
                          @RequestParam @NotNull Long doctorId,
                          @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime appointmentDateTime,
                          RedirectAttributes redirectAttributes,
                          Model model) {
        try {
            appointmentService.create(patientId, doctorId, new AppointmentRequestDto(appointmentDateTime));
        } catch (RuntimeException ex) {
            model.addAttribute("errorMessage", "Could not book the appointment: " + ex.getMessage());
            model.addAttribute("selectedPatientId", patientId);
            model.addAttribute("selectedDoctorId", doctorId);
            model.addAttribute("availableSlots", loadAvailableSlots(doctorId));
            return "appointments/form";
        }

        redirectAttributes.addFlashAttribute("successMessage", "Appointment booked.");
        return "redirect:/patients/" + patientId;
    }

    @GetMapping("/appointments/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        model.addAttribute("appointment", appointmentService.getById(id));
        return "appointments/edit";
    }

    @PostMapping("/appointments/{id}/update")
    public String updateFromForm(@PathVariable Long id,
                                  @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime appointmentDateTime,
                                  @RequestParam AppointmentStatus status,
                                  RedirectAttributes redirectAttributes) {
        appointmentService.update(id, new AppointmentUpdateDto(appointmentDateTime, status));
        redirectAttributes.addFlashAttribute("successMessage", "Appointment updated.");
        return "redirect:/appointments/" + id;
    }

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
