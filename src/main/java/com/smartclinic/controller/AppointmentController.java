package com.smartclinic.controller;

import com.smartclinic.model.Appointment;
import com.smartclinic.model.Patient;
import com.smartclinic.service.AppointmentService;
import com.smartclinic.service.CommonService;
import com.smartclinic.service.PatientService;
import java.time.LocalDateTime;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Map;

@RestController
@RequestMapping("/appointments")
public class AppointmentController {

    private final AppointmentService appointmentService;
    private final PatientService patientService;
    private final CommonService commonService;

    public AppointmentController(AppointmentService appointmentService,
                                 PatientService patientService,
                                 CommonService commonService) {
        this.appointmentService = appointmentService;
        this.patientService = patientService;
        this.commonService = commonService;
    }

    /** GET /appointments/{date}/{patientName}/{token} - doctor's appointments on a date (use "null" for no name filter). */
    @GetMapping("/{date}/{patientName}/{token}")
    public ResponseEntity<?> getAppointments(@PathVariable String date,
                                             @PathVariable String patientName,
                                             @PathVariable String token) {
        ResponseEntity<Map<String, String>> invalid = commonService.validateToken(token, "doctor");
        if (invalid != null) {
            return invalid;
        }
        try {
            String name = patientName.equalsIgnoreCase("null") ? null : patientName;
            return ResponseEntity.ok(appointmentService.getAppointments(name, LocalDate.parse(date), token));
        } catch (DateTimeParseException e) {
            return CommonService.message(HttpStatus.BAD_REQUEST, "Date must use the format yyyy-MM-dd");
        }
    }

    /** POST /appointments/{token} - a logged-in patient books an appointment. The patient comes from the token. */
    @PostMapping("/{token}")
    public ResponseEntity<Map<String, String>> bookAppointment(@RequestBody Appointment appointment,
                                                               @PathVariable String token) {
        ResponseEntity<Map<String, String>> invalid = commonService.validateToken(token, "patient");
        if (invalid != null) {
            return invalid;
        }
        if (appointment.getAppointmentTime() == null || !appointment.getAppointmentTime().isAfter(LocalDateTime.now())) {
            return CommonService.message(HttpStatus.BAD_REQUEST, "Choose a time in the future");
        }
        Patient patient = patientService.getPatientDetails(token);
        appointment.setPatient(patient);
        if (appointmentService.bookAppointment(appointment) == 1) {
            return CommonService.message(HttpStatus.CREATED, "Appointment booked successfully");
        }
        return CommonService.message(HttpStatus.CONFLICT, "That time slot is not available");
    }

    /** PUT /appointments/{token} - patient moves an appointment. */
    @PutMapping("/{token}")
    public ResponseEntity<Map<String, String>> updateAppointment(@RequestBody Appointment appointment,
                                                                 @PathVariable String token) {
        ResponseEntity<Map<String, String>> invalid = commonService.validateToken(token, "patient");
        if (invalid != null) {
            return invalid;
        }
        return appointmentService.updateAppointment(appointment, token);
    }

    /** DELETE /appointments/{id}/{token} - patient cancels an appointment. */
    @DeleteMapping("/{id}/{token}")
    public ResponseEntity<Map<String, String>> cancelAppointment(@PathVariable Long id, @PathVariable String token) {
        ResponseEntity<Map<String, String>> invalid = commonService.validateToken(token, "patient");
        if (invalid != null) {
            return invalid;
        }
        return appointmentService.cancelAppointment(id, token);
    }

    /** PUT /appointments/complete/{id}/{token} - doctor marks an appointment as completed. */
    @PutMapping("/complete/{id}/{token}")
    public ResponseEntity<Map<String, String>> complete(@PathVariable Long id, @PathVariable String token) {
        ResponseEntity<Map<String, String>> invalid = commonService.validateToken(token, "doctor");
        if (invalid != null) {
            return invalid;
        }
        return appointmentService.markCompleted(id, token);
    }
}
