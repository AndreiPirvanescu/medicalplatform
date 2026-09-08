package com.andrei.project.medicalplatform.controller;

import com.andrei.project.medicalplatform.service.AdminStatsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class AdminController {

    private final AdminStatsService adminStatsService;

    @GetMapping("/admin/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("stats", adminStatsService.getStats());
        return "admin/dashboard";
    }
}
