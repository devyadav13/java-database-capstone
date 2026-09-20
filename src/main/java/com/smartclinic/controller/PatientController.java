package com.smartclinic.controller;

import com.smartclinic.dto.Login;
import com.smartclinic.model.Appointment;
import com.smartclinic.model.Patient;
import com.smartclinic.service.CommonService;
import com.smartclinic.service.PatientService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/patient")
public class PatientController {

    private final PatientService patientService;
    private final CommonService commonService;

    public PatientController(PatientService patientService, CommonService commonService) {
        this.patientService = patientService;
        this.commonService = commonService;
    }

    /** POST /patient - sign up. */
    @PostMapping
    public ResponseEntity<Map<String, String>> createPatient(@RequestBody @Valid Patient patient) {
        int result = patientService.createPatient(patient);
        if (result == 1) {
            return CommonService.message(HttpStatus.CREATED, "Signup successful");
        }
        if (result == -1) {
            return CommonService.message(HttpStatus.CONFLICT, "A patient with this email or phone already exists");
        }
        return CommonService.message(HttpStatus.INTERNAL_SERVER_ERROR, "Signup failed");
    }

    /** POST /patient/login */
    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@RequestBody Login login) {
        return patientService.validatePatientLogin(login);
    }

    /** GET /patient/{token} - details of the logged-in patient. */
    @GetMapping("/{token}")
    public ResponseEntity<?> getPatient(@PathVariable String token) {
        ResponseEntity<Map<String, String>> invalid = commonService.validateToken(token, "patient");
        if (invalid != null) {
            return invalid;
        }
        Map<String, Object> body = new HashMap<>();
        body.put("patient", patientService.getPatientDetails(token));
        return ResponseEntity.ok(body);
    }

    /** GET /patient/{id}/{token} - all appointments of the patient. */
    @GetMapping("/{id}/{token}")
    public ResponseEntity<?> getPatientAppointments(@PathVariable Long id, @PathVariable String token) {
        ResponseEntity<Map<String, String>> invalid = commonService.validateToken(token, "patient");
        if (invalid != null) {
            return invalid;
        }
        return wrap(patientService.getPatientAppointments(id, token));
    }

    /**
     * GET /patient/filter/{condition}/{name}/{id}/{token}
     * condition: past | future | null.  name: doctor name or null.
     */
    @GetMapping("/filter/{condition}/{name}/{id}/{token}")
    public ResponseEntity<?> filterAppointments(@PathVariable String condition,
                                                @PathVariable String name,
                                                @PathVariable Long id,
                                                @PathVariable String token) {
        ResponseEntity<Map<String, String>> invalid = commonService.validateToken(token, "patient");
        if (invalid != null) {
            return invalid;
        }
        boolean hasCondition = !condition.equalsIgnoreCase("null");
        boolean hasName = !name.equalsIgnoreCase("null");

        List<Appointment> result;
        if (hasCondition && hasName) {
            result = patientService.filterByDoctorAndCondition(condition, name, id, token);
        } else if (hasCondition) {
            result = patientService.filterByCondition(condition, id, token);
        } else if (hasName) {
            result = patientService.filterByDoctor(name, id, token);
        } else {
            result = patientService.getPatientAppointments(id, token);
        }
        return wrap(result);
    }

    private ResponseEntity<?> wrap(List<Appointment> appointments) {
        if (appointments == null) {
            return CommonService.message(HttpStatus.FORBIDDEN, "You can only view your own appointments");
        }
        Map<String, Object> body = new HashMap<>();
        body.put("appointments", appointments);
        return ResponseEntity.ok(body);
    }
}
