package com.andrei.project.medicalplatform.controller;

import com.andrei.project.medicalplatform.dto.medicalrecord.MedicalHistoryEntryDto;
import com.andrei.project.medicalplatform.dto.medicalrecord.MedicalRecordRequestDto;
import com.andrei.project.medicalplatform.dto.medicalrecord.MedicalRecordResponseDto;
import com.andrei.project.medicalplatform.dto.medicalrecord.MedicalRecordUpdateDto;
import com.andrei.project.medicalplatform.dto.patient.PatientResponseDto;
import com.andrei.project.medicalplatform.service.MedicalRecordService;
import com.andrei.project.medicalplatform.service.PatientService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@Validated
@RequiredArgsConstructor
public class MedicalRecordController {

    private final MedicalRecordService medicalRecordService;
    private final PatientService patientService;

    @GetMapping("/patients/{patientId}/medical-record")
    public String view(@PathVariable Long patientId, Model model) {
        PatientResponseDto patient = patientService.getById(patientId);
        model.addAttribute("patient", patient);

        try {
            MedicalRecordResponseDto record = medicalRecordService.getByPatient(patientId);
            List<MedicalHistoryEntryDto> history = medicalRecordService.getHistory(patientId);
            model.addAttribute("record", record);
            model.addAttribute("history", history);
        } catch (EntityNotFoundException ex) {
            model.addAttribute("record", null);
        }

        return "medical-records/view";
    }

    @GetMapping("/patients/{patientId}/medical-record/new")
    public String newForm(@PathVariable Long patientId, Model model) {
        model.addAttribute("patient", patientService.getById(patientId));
        return "medical-records/form";
    }

    @PostMapping("/patients/{patientId}/medical-record")
    public String create(@PathVariable Long patientId,
                          @RequestParam(required = false) String notes,
                          RedirectAttributes redirectAttributes) {
        medicalRecordService.create(patientId, new MedicalRecordRequestDto(notes));
        redirectAttributes.addFlashAttribute("successMessage", "Medical record created.");
        return "redirect:/patients/" + patientId + "/medical-record";
    }

    @GetMapping("/medical-records/{id}/edit")
    public String editForm(@PathVariable Long id, @RequestParam Long patientId, Model model) {
        model.addAttribute("record", medicalRecordService.getById(id));
        model.addAttribute("patient", patientService.getById(patientId));
        return "medical-records/edit";
    }

    @PostMapping("/medical-records/{id}/update")
    public String updateFromForm(@PathVariable Long id,
                                  @RequestParam Long patientId,
                                  @RequestParam @jakarta.validation.constraints.NotBlank String notes,
                                  RedirectAttributes redirectAttributes) {
        medicalRecordService.update(id, new MedicalRecordUpdateDto(notes));
        redirectAttributes.addFlashAttribute("successMessage", "Medical record updated.");
        return "redirect:/patients/" + patientId + "/medical-record";
    }
}
