package com.andrei.project.medicalplatform.controller;

import com.andrei.project.medicalplatform.dto.admin.AdminStatsDto;
import com.andrei.project.medicalplatform.service.AdminStatsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Admin controller: your original JSON API (unchanged, at /api/admin/stats,
 * now with explicit @ResponseBody - same pattern as the other controllers)
 * plus the Thymeleaf page for Feature 9 (Admin Dashboard).
 */
@Controller
@RequiredArgsConstructor
public class AdminController {

    private final AdminStatsService adminStatsService;

    @GetMapping("/api/admin/stats")
    @ResponseBody
    public ResponseEntity<AdminStatsDto> getStats() {
        return ResponseEntity.ok(adminStatsService.getStats());
    }

    // GET /admin/dashboard
    @GetMapping("/admin/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("stats", adminStatsService.getStats());
        return "admin/dashboard";
    }
}
