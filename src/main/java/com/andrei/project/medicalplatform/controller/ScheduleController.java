package com.andrei.project.medicalplatform.controller;

import com.andrei.project.medicalplatform.dto.doctor.DoctorResponseDto;
import com.andrei.project.medicalplatform.dto.schedule.ScheduleRequestDto;
import com.andrei.project.medicalplatform.dto.schedule.ScheduleResponseDto;
import com.andrei.project.medicalplatform.dto.schedule.ScheduleUpdateDto;
import com.andrei.project.medicalplatform.service.DoctorService;
import com.andrei.project.medicalplatform.service.ScheduleService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class ScheduleController {

    private final ScheduleService scheduleService; // TODO: create this service if it doesn't exist yet
    private final DoctorService doctorService;

    @GetMapping("/doctors/{doctorId}/schedule")
    public String list(@PathVariable Long doctorId, Model model) {
        DoctorResponseDto doctor = doctorService.getById(doctorId);
        List<ScheduleResponseDto> slots = scheduleService.getByDoctor(doctorId);

        model.addAttribute("doctor", doctor);
        model.addAttribute("slots", slots);
        model.addAttribute("newSlot", new ScheduleFormValues());
        return "schedules/list";
    }

    @PostMapping("/doctors/{doctorId}/schedule")
    public String create(@PathVariable Long doctorId,
                          @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
                          @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime,
                          Model model,
                          RedirectAttributes redirectAttributes) {
        try {
            scheduleService.create(doctorId, new ScheduleRequestDto(startTime, endTime));
        } catch (RuntimeException ex) {
            model.addAttribute("errorMessage", "Could not add slot: " + ex.getMessage());
            model.addAttribute("doctor", doctorService.getById(doctorId));
            model.addAttribute("slots", scheduleService.getByDoctor(doctorId));
            model.addAttribute("newSlot", new ScheduleFormValues());
            return "schedules/list";
        }
        redirectAttributes.addFlashAttribute("successMessage", "Time slot added.");
        return "redirect:/doctors/" + doctorId + "/schedule";
    }

    @GetMapping("/schedules/{id}/edit")
    public String editForm(@PathVariable Long id, @RequestParam Long doctorId, Model model) {
        ScheduleResponseDto slot = scheduleService.getByDoctor(doctorId).stream()
                .filter(s -> s.id().equals(id))
                .findFirst()
                .orElseThrow(() -> new jakarta.persistence.EntityNotFoundException("Schedule not found with id " + id));
        model.addAttribute("slot", slot);
        return "schedules/edit";
    }

    @PostMapping("/schedules/{id}/update")
    public String updateFromForm(@PathVariable Long id,
                                  @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
                                  @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime,
                                  @RequestParam(defaultValue = "false") boolean available,
                                  RedirectAttributes redirectAttributes) {
        ScheduleResponseDto updated = scheduleService.update(id, new ScheduleUpdateDto(startTime, endTime, available));
        redirectAttributes.addFlashAttribute("successMessage", "Time slot updated.");
        return "redirect:/doctors/" + updated.doctorId() + "/schedule";
    }

    @PostMapping("/schedules/{id}/delete")
    public String deleteFromForm(@PathVariable Long id,
                                  @RequestParam Long doctorId,
                                  RedirectAttributes redirectAttributes) {
        scheduleService.delete(id);
        redirectAttributes.addFlashAttribute("successMessage", "Time slot removed.");
        return "redirect:/doctors/" + doctorId + "/schedule";
    }

    public static class ScheduleFormValues {
        private LocalDateTime startTime;
        private LocalDateTime endTime;

        public LocalDateTime getStartTime() { return startTime; }
        public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }
        public LocalDateTime getEndTime() { return endTime; }
        public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }
    }
}
