package com.andrei.project.medicalplatform.controller;

import com.andrei.project.medicalplatform.dto.medication.MedicationHistoryEntryDto;
import com.andrei.project.medicalplatform.dto.medication.MedicationResponseDto;
import com.andrei.project.medicalplatform.dto.patient.PatientResponseDto;
import com.andrei.project.medicalplatform.service.MedicationService;
import com.andrei.project.medicalplatform.service.PatientService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class MedicationController {

    private final MedicationService medicationService;
    private final PatientService patientService;

    @GetMapping("/medications")
    public String list(@RequestParam(required = false) String name,
                        @PageableDefault(size = 20, sort = "name") Pageable pageable,
                        Model model) {
        Page<MedicationResponseDto> medications = medicationService.getAll(name, null, pageable);
        model.addAttribute("medications", medications);
        return "medications/list";
    }

    @GetMapping("/medications/{id}")
    public String view(@PathVariable Long id, Model model) {
        model.addAttribute("medication", medicationService.getById(id));
        return "medications/view";
    }

    @GetMapping("/patients/{patientId}/medication-history")
    public String patientHistory(@PathVariable Long patientId,
                                  @RequestParam(required = false) String name,
                                  Model model) {
        PatientResponseDto patient = patientService.getById(patientId);
        List<MedicationHistoryEntryDto> history = medicationService.getPatientHistory(patientId, name);

        model.addAttribute("patient", patient);
        model.addAttribute("history", history);
        model.addAttribute("nameFilter", name);
        return "medications/patient-history";
    }
}
