package com.smartclinic.service;

import com.smartclinic.model.Appointment;
import com.smartclinic.model.Prescription;
import com.smartclinic.repository.AppointmentRepository;
import com.smartclinic.repository.PrescriptionRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class PrescriptionService {

    private final PrescriptionRepository prescriptionRepository;
    private final AppointmentRepository appointmentRepository;
    private final TokenService tokenService;

    public PrescriptionService(PrescriptionRepository prescriptionRepository,
                               AppointmentRepository appointmentRepository,
                               TokenService tokenService) {
        this.prescriptionRepository = prescriptionRepository;
        this.appointmentRepository = appointmentRepository;
        this.tokenService = tokenService;
    }

    /** Saves a prescription for an appointment that belongs to the logged-in doctor. */
    public ResponseEntity<Map<String, String>> savePrescription(Prescription prescription, String token) {
        try {
            Optional<Appointment> appointment = appointmentRepository.findById(prescription.getAppointmentId());
            if (appointment.isEmpty()) {
                return CommonService.message(HttpStatus.NOT_FOUND, "Appointment not found");
            }
            String email = tokenService.extractIdentifier(token);
            if (!appointment.get().getDoctor().getEmail().equals(email)) {
                return CommonService.message(HttpStatus.FORBIDDEN, "This appointment belongs to another doctor");
            }
            if (!prescriptionRepository.findByAppointmentId(prescription.getAppointmentId()).isEmpty()) {
                return CommonService.message(HttpStatus.BAD_REQUEST, "A prescription already exists for this appointment");
            }
            prescription.setId(null);
            prescriptionRepository.save(prescription);
            return CommonService.message(HttpStatus.CREATED, "Prescription saved");
        } catch (Exception e) {
            return CommonService.message(HttpStatus.INTERNAL_SERVER_ERROR, "Could not save the prescription");
        }
    }

    public ResponseEntity<Map<String, Object>> getPrescription(Long appointmentId) {
        List<Prescription> list = prescriptionRepository.findByAppointmentId(appointmentId);
        Map<String, Object> body = new HashMap<>();
        body.put("prescription", list);
        return ResponseEntity.ok(body);
    }
}
