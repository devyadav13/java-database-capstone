package com.smartclinic.controller;

import com.smartclinic.service.CommonService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/** MVC controller: serves the Thymeleaf dashboards only when the token is valid for the role. */
@Controller
public class DashboardController {

    private final CommonService commonService;

    public DashboardController(CommonService commonService) {
        this.commonService = commonService;
    }

    @GetMapping("/adminDashboard/{token}")
    public String adminDashboard(@PathVariable String token) {
        return commonService.validateToken(token, "admin") == null ? "admin/adminDashboard" : "redirect:/";
    }

    @GetMapping("/doctorDashboard/{token}")
    public String doctorDashboard(@PathVariable String token) {
        return commonService.validateToken(token, "doctor") == null ? "doctor/doctorDashboard" : "redirect:/";
    }
}
