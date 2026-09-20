package com.smartclinic.controller;

import com.smartclinic.dto.Login;
import com.smartclinic.service.CommonService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/admin")
public class AdminController {

    private final CommonService commonService;

    public AdminController(CommonService commonService) {
        this.commonService = commonService;
    }

    /** POST /admin/login - body: {"identifier": "admin", "password": "..."} */
    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> adminLogin(@RequestBody Login login) {
        return commonService.validateAdmin(login);
    }
}
