package com.smartclinic.service;

import com.smartclinic.dto.Login;
import com.smartclinic.model.Admin;
import com.smartclinic.repository.AdminRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/** Shared helpers: structured responses, token checks and admin login. */
@Service
public class CommonService {

    private final TokenService tokenService;
    private final AdminRepository adminRepository;
    private final PasswordEncoder passwordEncoder;

    public CommonService(TokenService tokenService, AdminRepository adminRepository, PasswordEncoder passwordEncoder) {
        this.tokenService = tokenService;
        this.adminRepository = adminRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public static ResponseEntity<Map<String, String>> message(HttpStatus status, String text) {
        Map<String, String> body = new HashMap<>();
        body.put("message", text);
        return ResponseEntity.status(status).body(body);
    }

    /** Returns null when the token is valid for the role, otherwise a 401 response. */
    public ResponseEntity<Map<String, String>> validateToken(String token, String user) {
        if (!tokenService.validateToken(token, user)) {
            return message(HttpStatus.UNAUTHORIZED, "Invalid or expired token");
        }
        return null;
    }

    public ResponseEntity<Map<String, String>> validateAdmin(Login login) {
        try {
            Admin admin = adminRepository.findByUsername(login.getIdentifier());
            if (admin == null || !passwordEncoder.matches(login.getPassword(), admin.getPassword())) {
                return message(HttpStatus.UNAUTHORIZED, "Invalid username or password");
            }
            Map<String, String> body = new HashMap<>();
            body.put("token", tokenService.generateToken(admin.getUsername()));
            return ResponseEntity.ok(body);
        } catch (Exception e) {
            return message(HttpStatus.INTERNAL_SERVER_ERROR, "Login failed. Please try again.");
        }
    }
}
