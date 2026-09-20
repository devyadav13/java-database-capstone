package com.smartclinic.service;

import com.smartclinic.dto.Login;
import com.smartclinic.model.Appointment;
import com.smartclinic.model.Patient;
import com.smartclinic.repository.AppointmentRepository;
import com.smartclinic.repository.PatientRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class PatientService {

    private final PatientRepository patientRepository;
    private final AppointmentRepository appointmentRepository;
    private final TokenService tokenService;
    private final PasswordEncoder passwordEncoder;

    public PatientService(PatientRepository patientRepository,
                          AppointmentRepository appointmentRepository,
                          TokenService tokenService,
                          PasswordEncoder passwordEncoder) {
        this.patientRepository = patientRepository;
        this.appointmentRepository = appointmentRepository;
        this.tokenService = tokenService;
        this.passwordEncoder = passwordEncoder;
    }

    /** @return 1 created, -1 email or phone already used, 0 internal error */
    public int createPatient(Patient patient) {
        try {
            if (patientRepository.findByEmailOrPhone(patient.getEmail(), patient.getPhone()) != null) {
                return -1;
            }
            patient.setId(null);
            patient.setPassword(passwordEncoder.encode(patient.getPassword()));
            patientRepository.save(patient);
            return 1;
        } catch (Exception e) {
            return 0;
        }
    }

    public ResponseEntity<Map<String, String>> validatePatientLogin(Login login) {
        try {
            Patient patient = patientRepository.findByEmail(login.getIdentifier());
            if (patient == null || !passwordEncoder.matches(login.getPassword(), patient.getPassword())) {
                return CommonService.message(HttpStatus.UNAUTHORIZED, "Invalid email or password");
            }
            Map<String, String> body = new HashMap<>();
            body.put("token", tokenService.generateToken(patient.getEmail()));
            return ResponseEntity.ok(body);
        } catch (Exception e) {
            return CommonService.message(HttpStatus.INTERNAL_SERVER_ERROR, "Login failed. Please try again.");
        }
    }

    public Patient getPatientDetails(String token) {
        return patientRepository.findByEmail(tokenService.extractIdentifier(token));
    }

    /** Returns all appointments for the patient, or null if the token does not belong to that patient. */
    public List<Appointment> getPatientAppointments(Long patientId, String token) {
        if (!ownsRecord(patientId, token)) {
            return null;
        }
        return appointmentRepository.findByPatient_IdOrderByAppointmentTimeAsc(patientId);
    }

    /** condition: "past" (completed) or "future" (scheduled). */
    public List<Appointment> filterByCondition(String condition, Long patientId, String token) {
        if (!ownsRecord(patientId, token)) {
            return null;
        }
        return appointmentRepository.findByPatient_IdAndStatusOrderByAppointmentTimeAsc(patientId, statusOf(condition));
    }

    public List<Appointment> filterByDoctor(String doctorName, Long patientId, String token) {
        if (!ownsRecord(patientId, token)) {
            return null;
        }
        return appointmentRepository.filterByDoctorName(patientId, doctorName);
    }

    public List<Appointment> filterByDoctorAndCondition(String condition, String doctorName, Long patientId, String token) {
        if (!ownsRecord(patientId, token)) {
            return null;
        }
        return appointmentRepository.filterByDoctorNameAndStatus(patientId, doctorName, statusOf(condition));
    }

    private boolean ownsRecord(Long patientId, String token) {
        Patient patient = patientRepository.findByEmail(tokenService.extractIdentifier(token));
        return patient != null && patient.getId().equals(patientId);
    }

    private int statusOf(String condition) {
        return "past".equalsIgnoreCase(condition) ? Appointment.COMPLETED : Appointment.SCHEDULED;
    }
}
