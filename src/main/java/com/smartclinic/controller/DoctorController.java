package com.smartclinic.controller;

import com.smartclinic.dto.Login;
import com.smartclinic.model.Doctor;
import com.smartclinic.service.CommonService;
import com.smartclinic.service.DoctorService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/doctor")
public class DoctorController {

    private final DoctorService doctorService;
    private final CommonService commonService;

    public DoctorController(DoctorService doctorService, CommonService commonService) {
        this.doctorService = doctorService;
        this.commonService = commonService;
    }

    /** GET /doctor - list all doctors (public, used by the patient search page). */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getDoctors() {
        Map<String, Object> body = new HashMap<>();
        body.put("doctors", doctorService.getDoctors());
        return ResponseEntity.ok(body);
    }

    /** GET /doctor/availability/{user}/{doctorId}/{date}/{token} - free slots on a date (date = yyyy-MM-dd). */
    @GetMapping("/availability/{user}/{doctorId}/{date}/{token}")
    public ResponseEntity<?> getDoctorAvailability(@PathVariable String user,
                                                   @PathVariable Long doctorId,
                                                   @PathVariable String date,
                                                   @PathVariable String token) {
        if (!user.equals("patient") && !user.equals("doctor")) {
            return CommonService.message(HttpStatus.BAD_REQUEST, "User must be 'patient' or 'doctor'");
        }
        ResponseEntity<Map<String, String>> invalid = commonService.validateToken(token, user);
        if (invalid != null) {
            return invalid;
        }
        try {
            Map<String, Object> body = new HashMap<>();
            body.put("availability", doctorService.getDoctorAvailability(doctorId, LocalDate.parse(date)));
            return ResponseEntity.ok(body);
        } catch (DateTimeParseException e) {
            return CommonService.message(HttpStatus.BAD_REQUEST, "Date must use the format yyyy-MM-dd");
        }
    }

    /** POST /doctor/{token} - admin adds a doctor. */
    @PostMapping("/{token}")
    public ResponseEntity<Map<String, String>> saveDoctor(@RequestBody @Valid Doctor doctor, @PathVariable String token) {
        ResponseEntity<Map<String, String>> invalid = commonService.validateToken(token, "admin");
        if (invalid != null) {
            return invalid;
        }
        int result = doctorService.saveDoctor(doctor);
        if (result == 1) {
            return CommonService.message(HttpStatus.CREATED, "Doctor added successfully");
        }
        if (result == -1) {
            return CommonService.message(HttpStatus.CONFLICT, "A doctor with this email already exists");
        }
        return CommonService.message(HttpStatus.INTERNAL_SERVER_ERROR, "Could not add the doctor");
    }

    /** POST /doctor/login */
    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> doctorLogin(@RequestBody Login login) {
        return doctorService.validateDoctor(login);
    }

    /** PUT /doctor/{token} - admin updates a doctor. */
    @PutMapping("/{token}")
    public ResponseEntity<Map<String, String>> updateDoctor(@RequestBody Doctor doctor, @PathVariable String token) {
        ResponseEntity<Map<String, String>> invalid = commonService.validateToken(token, "admin");
        if (invalid != null) {
            return invalid;
        }
        int result = doctorService.updateDoctor(doctor);
        if (result == 1) {
            return CommonService.message(HttpStatus.OK, "Doctor updated");
        }
        if (result == -1) {
            return CommonService.message(HttpStatus.NOT_FOUND, "Doctor not found");
        }
        return CommonService.message(HttpStatus.INTERNAL_SERVER_ERROR, "Could not update the doctor");
    }

    /** DELETE /doctor/{id}/{token} - admin deletes a doctor. */
    @DeleteMapping("/{id}/{token}")
    public ResponseEntity<Map<String, String>> deleteDoctor(@PathVariable Long id, @PathVariable String token) {
        ResponseEntity<Map<String, String>> invalid = commonService.validateToken(token, "admin");
        if (invalid != null) {
            return invalid;
        }
        int result = doctorService.deleteDoctor(id);
        if (result == 1) {
            return CommonService.message(HttpStatus.OK, "Doctor deleted");
        }
        if (result == -1) {
            return CommonService.message(HttpStatus.NOT_FOUND, "Doctor not found");
        }
        return CommonService.message(HttpStatus.INTERNAL_SERVER_ERROR, "Could not delete the doctor");
    }

    /**
     * GET /doctor/filter/{name}/{time}/{speciality}
     * Use the word "null" for any filter you want to skip. time is AM or PM.
     */
    @GetMapping("/filter/{name}/{time}/{speciality}")
    public ResponseEntity<Map<String, Object>> filterDoctors(@PathVariable String name,
                                                             @PathVariable String time,
                                                             @PathVariable String speciality) {
        Map<String, Object> body = new HashMap<>();
        body.put("doctors", doctorService.filterDoctors(clean(name), clean(speciality), clean(time)));
        return ResponseEntity.ok(body);
    }

    private static String clean(String value) {
        return (value == null || value.equalsIgnoreCase("null")) ? null : value;
    }
}
