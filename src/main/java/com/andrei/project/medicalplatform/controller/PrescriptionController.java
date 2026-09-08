package com.andrei.project.medicalplatform.controller;

import com.andrei.project.medicalplatform.dto.appointment.AppointmentResponseDto;
import com.andrei.project.medicalplatform.dto.medication.MedicationResponseDto;
import com.andrei.project.medicalplatform.dto.prescription.PrescriptionRequestDto;
import com.andrei.project.medicalplatform.dto.prescription.PrescriptionResponseDto;
import com.andrei.project.medicalplatform.dto.prescription.PrescriptionUpdateDto;
import com.andrei.project.medicalplatform.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.List;

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

    @GetMapping("/prescriptions/{id}")
    public String view(@PathVariable Long id, Model model) {
        model.addAttribute("prescription", prescriptionService.getById(id));
        return "prescriptions/view";
    }

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

    @GetMapping("/prescriptions/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        PrescriptionResponseDto prescription = prescriptionService.getById(id);
        model.addAttribute("prescription", prescription);
        model.addAttribute("selectedMedicationIds",
                prescription.medications().stream().map(m -> m.id()).toList());
        return "prescriptions/edit";
    }

    @PostMapping("/prescriptions/{id}/update")
    public String updateFromForm(@PathVariable Long id,
                                  @RequestParam(required = false) String notes,
                                  @RequestParam(required = false) List<Long> medicationIds,
                                  RedirectAttributes redirectAttributes) {
        prescriptionService.update(id, new PrescriptionUpdateDto(notes, medicationIds));
        redirectAttributes.addFlashAttribute("successMessage", "Prescription updated.");
        return "redirect:/prescriptions/" + id;
    }

    @PostMapping("/prescriptions/{id}/delete")
    public String deleteFromForm(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        prescriptionService.delete(id);
        redirectAttributes.addFlashAttribute("successMessage", "Prescription deleted.");
        return "redirect:/prescriptions";
    }

    @PostMapping("/prescriptions/{id}/medications")
    public String addMedicationFromForm(@PathVariable Long id,
                                         @RequestParam Long medicationId,
                                         RedirectAttributes redirectAttributes) {
        medicationService.addToPrescription(id, medicationId);
        redirectAttributes.addFlashAttribute("successMessage", "Medication added.");
        return "redirect:/prescriptions/" + id;
    }

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
