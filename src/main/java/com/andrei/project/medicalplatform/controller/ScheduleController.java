package com.andrei.project.medicalplatform.controller;

import com.andrei.project.medicalplatform.dto.doctor.DoctorResponseDto;
import com.andrei.project.medicalplatform.dto.schedule.ScheduleRequestDto;
import com.andrei.project.medicalplatform.dto.schedule.ScheduleResponseDto;
import com.andrei.project.medicalplatform.dto.schedule.ScheduleUpdateDto;
import com.andrei.project.medicalplatform.service.DoctorService;
import com.andrei.project.medicalplatform.service.ScheduleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Feature 5 - Doctor Schedule Management.
 *
 * UNLIKE the other four controllers, no ScheduleController was given to me,
 * only the three DTOs (ScheduleRequestDto/ScheduleUpdateDto/ScheduleResponseDto)
 * and the action list from the feature spec:
 *   POST   /api/schedules/for-doctor/{doctorId}
 *   DELETE /api/schedules/{id}
 *   PUT    /api/schedules/{id}
 *   GET    /api/schedules/by-doctor/{doctorId}
 *   GET    /api/schedules/by-doctor/{doctorId}/available
 *
 * So this whole file is a scaffold, written from scratch in the same
 * @Controller + explicit @ResponseBody hybrid style as your other four
 * controllers, so the JSON API and the Thymeleaf pages live together
 * consistently across the project.
 *
 * Now matched against your real ScheduleService: create/update/delete/
 * getByDoctor/getAvailableByDoctor(doctorId, from, to). It has no getById(id),
 * so the edit-slot page below takes doctorId as an extra query param and
 * finds the slot inside getByDoctor(doctorId) instead of adding a new method
 * to your service - add a real getById if you'd rather do it that way.
 *
 * Overlap/time-range validation is enforced inside ScheduleService
 * (ScheduleOverlapException / InvalidScheduleTimeException) - the create/
 * update view methods below just catch RuntimeException broadly and surface
 * the message as a form error. Narrow that catch to those two exception
 * types if you want different handling per case.
 */
@Controller
@RequiredArgsConstructor
public class ScheduleController {

    private final ScheduleService scheduleService; // TODO: create this service if it doesn't exist yet
    private final DoctorService doctorService;

    // =========================================================
    // JSON API - /api/schedules/...
    // =========================================================

    @PostMapping("/api/schedules/for-doctor/{doctorId}")
    @ResponseBody
    public ResponseEntity<ScheduleResponseDto> create(
            @PathVariable Long doctorId,
            @Valid @RequestBody ScheduleRequestDto request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(scheduleService.create(doctorId, request));
    }

    @DeleteMapping("/api/schedules/{id}")
    @ResponseBody
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        scheduleService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/api/schedules/{id}")
    @ResponseBody
    public ResponseEntity<ScheduleResponseDto> update(
            @PathVariable Long id,
            @Valid @RequestBody ScheduleUpdateDto request) {
        return ResponseEntity.ok(scheduleService.update(id, request));
    }

    @GetMapping("/api/schedules/by-doctor/{doctorId}")
    @ResponseBody
    public ResponseEntity<List<ScheduleResponseDto>> getByDoctor(@PathVariable Long doctorId) {
        return ResponseEntity.ok(scheduleService.getByDoctor(doctorId));
    }

    @GetMapping("/api/schedules/by-doctor/{doctorId}/available")
    @ResponseBody
    public ResponseEntity<List<ScheduleResponseDto>> getAvailable(
            @PathVariable Long doctorId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to) {
        return ResponseEntity.ok(scheduleService.getAvailableByDoctor(doctorId, from, to));
    }

    // =========================================================
    // Thymeleaf pages
    // =========================================================

    // GET /doctors/{doctorId}/schedule - the doctor's full list of slots
    // (mirrors GET /api/schedules/by-doctor/{doctorId}), with an inline form
    // to add a new one.
    @GetMapping("/doctors/{doctorId}/schedule")
    public String list(@PathVariable Long doctorId, Model model) {
        DoctorResponseDto doctor = doctorService.getById(doctorId);
        List<ScheduleResponseDto> slots = scheduleService.getByDoctor(doctorId);

        model.addAttribute("doctor", doctor);
        model.addAttribute("slots", slots);
        model.addAttribute("newSlot", new ScheduleFormValues());
        return "schedules/list";
    }

    // POST /doctors/{doctorId}/schedule  (view-form equivalent of POST /api/schedules/for-doctor/{doctorId})
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

    // GET /schedules/{id}/edit?doctorId=  (doctorId is required here only because
    // ScheduleService has no getById(id) - it's used to look the slot up via
    // getByDoctor instead; see schedules/list.html's edit link, which passes it)
    @GetMapping("/schedules/{id}/edit")
    public String editForm(@PathVariable Long id, @RequestParam Long doctorId, Model model) {
        ScheduleResponseDto slot = scheduleService.getByDoctor(doctorId).stream()
                .filter(s -> s.id().equals(id))
                .findFirst()
                .orElseThrow(() -> new jakarta.persistence.EntityNotFoundException("Schedule not found with id " + id));
        model.addAttribute("slot", slot);
        return "schedules/edit";
    }

    // POST /schedules/{id}/update  (view-form equivalent of PUT /api/schedules/{id})
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

    // POST /schedules/{id}/delete  (view-form equivalent of DELETE /api/schedules/{id})
    @PostMapping("/schedules/{id}/delete")
    public String deleteFromForm(@PathVariable Long id,
                                  @RequestParam Long doctorId,
                                  RedirectAttributes redirectAttributes) {
        // doctorId is passed through as a hidden field purely so we know where to
        // redirect back to - swap for scheduleService.getById(id).doctorId() first
        // if you'd rather not thread it through the form.
        scheduleService.delete(id);
        redirectAttributes.addFlashAttribute("successMessage", "Time slot removed.");
        return "redirect:/doctors/" + doctorId + "/schedule";
    }

    /** Empty holder so schedules/list.html has something to th:object the "add slot" form against. */
    public static class ScheduleFormValues {
        private LocalDateTime startTime;
        private LocalDateTime endTime;

        public LocalDateTime getStartTime() { return startTime; }
        public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }
        public LocalDateTime getEndTime() { return endTime; }
        public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }
    }
}
