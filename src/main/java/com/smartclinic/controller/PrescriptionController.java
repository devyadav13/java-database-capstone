package com.smartclinic.controller;

import com.smartclinic.model.Prescription;
import com.smartclinic.service.CommonService;
import com.smartclinic.service.PrescriptionService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/prescription")
public class PrescriptionController {

    private final PrescriptionService prescriptionService;
    private final CommonService commonService;

    public PrescriptionController(PrescriptionService prescriptionService, CommonService commonService) {
        this.prescriptionService = prescriptionService;
        this.commonService = commonService;
    }

    /** POST /prescription/{token} - a doctor saves a prescription (stored in MongoDB). */
    @PostMapping("/{token}")
    public ResponseEntity<Map<String, String>> savePrescription(@RequestBody @Valid Prescription prescription,
                                                                @PathVariable String token) {
        ResponseEntity<Map<String, String>> invalid = commonService.validateToken(token, "doctor");
        if (invalid != null) {
            return invalid;
        }
        return prescriptionService.savePrescription(prescription, token);
    }

    /** GET /prescription/{appointmentId}/{token} - a doctor views the prescription for an appointment. */
    @GetMapping("/{appointmentId}/{token}")
    public ResponseEntity<?> getPrescription(@PathVariable Long appointmentId, @PathVariable String token) {
        ResponseEntity<Map<String, String>> invalid = commonService.validateToken(token, "doctor");
        if (invalid != null) {
            return invalid;
        }
        return prescriptionService.getPrescription(appointmentId);
    }
}
