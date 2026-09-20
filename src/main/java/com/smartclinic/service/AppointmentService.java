package com.smartclinic.service;

import com.smartclinic.model.Appointment;
import com.smartclinic.model.Doctor;
import com.smartclinic.model.Patient;
import com.smartclinic.repository.AppointmentRepository;
import com.smartclinic.repository.DoctorRepository;
import com.smartclinic.repository.PatientRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class AppointmentService {

    private static final DateTimeFormatter HH_MM = DateTimeFormatter.ofPattern("HH:mm");

    private final AppointmentRepository appointmentRepository;
    private final DoctorRepository doctorRepository;
    private final PatientRepository patientRepository;
    private final TokenService tokenService;

    public AppointmentService(AppointmentRepository appointmentRepository,
                              DoctorRepository doctorRepository,
                              PatientRepository patientRepository,
                              TokenService tokenService) {
        this.appointmentRepository = appointmentRepository;
        this.doctorRepository = doctorRepository;
        this.patientRepository = patientRepository;
        this.tokenService = tokenService;
    }

    /**
     * Books an appointment after checking the doctor offers the slot and it is free.
     * @return 1 booked, 0 doctor/patient missing or slot unavailable
     */
    public int bookAppointment(Appointment appointment) {
        try {
            if (appointment.getDoctor() == null || appointment.getPatient() == null
                    || appointment.getDoctor().getId() == null || appointment.getPatient().getId() == null) {
                return 0;
            }
            Optional<Doctor> doctor = doctorRepository.findById(appointment.getDoctor().getId());
            Optional<Patient> patient = patientRepository.findById(appointment.getPatient().getId());
            if (doctor.isEmpty() || patient.isEmpty()) {
                return 0;
            }
            if (!isSlotFree(doctor.get(), appointment.getAppointmentTime(), null)) {
                return 0;
            }
            appointment.setId(null);
            appointment.setDoctor(doctor.get());
            appointment.setPatient(patient.get());
            appointment.setStatus(Appointment.SCHEDULED);
            appointmentRepository.save(appointment);
            return 1;
        } catch (Exception e) {
            return 0;
        }
    }

    /** Lets the owning patient move an appointment to another free slot. */
    public ResponseEntity<Map<String, String>> updateAppointment(Appointment updated, String token) {
        if (updated.getId() == null) {
            return CommonService.message(HttpStatus.BAD_REQUEST, "Appointment id is required");
        }
        Optional<Appointment> existing = appointmentRepository.findById(updated.getId());
        if (existing.isEmpty()) {
            return CommonService.message(HttpStatus.NOT_FOUND, "Appointment not found");
        }
        Appointment appointment = existing.get();
        String email = tokenService.extractIdentifier(token);
        if (!appointment.getPatient().getEmail().equals(email)) {
            return CommonService.message(HttpStatus.FORBIDDEN, "You can only change your own appointments");
        }
        LocalDateTime newTime = updated.getAppointmentTime();
        if (newTime == null || !newTime.isAfter(LocalDateTime.now())) {
            return CommonService.message(HttpStatus.BAD_REQUEST, "Choose a time in the future");
        }
        if (!isSlotFree(appointment.getDoctor(), newTime, appointment.getId())) {
            return CommonService.message(HttpStatus.CONFLICT, "That time slot is not available");
        }
        appointment.setAppointmentTime(newTime);
        appointmentRepository.save(appointment);
        return CommonService.message(HttpStatus.OK, "Appointment updated");
    }

    /** Lets the owning patient cancel an appointment. */
    public ResponseEntity<Map<String, String>> cancelAppointment(Long id, String token) {
        Optional<Appointment> existing = appointmentRepository.findById(id);
        if (existing.isEmpty()) {
            return CommonService.message(HttpStatus.NOT_FOUND, "Appointment not found");
        }
        String email = tokenService.extractIdentifier(token);
        if (!existing.get().getPatient().getEmail().equals(email)) {
            return CommonService.message(HttpStatus.FORBIDDEN, "You can only cancel your own appointments");
        }
        appointmentRepository.delete(existing.get());
        return CommonService.message(HttpStatus.OK, "Appointment cancelled");
    }

    /** Lets the assigned doctor mark an appointment as completed. */
    public ResponseEntity<Map<String, String>> markCompleted(Long id, String token) {
        Optional<Appointment> existing = appointmentRepository.findById(id);
        if (existing.isEmpty()) {
            return CommonService.message(HttpStatus.NOT_FOUND, "Appointment not found");
        }
        String email = tokenService.extractIdentifier(token);
        if (!existing.get().getDoctor().getEmail().equals(email)) {
            return CommonService.message(HttpStatus.FORBIDDEN, "This appointment belongs to another doctor");
        }
        appointmentRepository.markCompleted(id);
        return CommonService.message(HttpStatus.OK, "Appointment marked as completed");
    }

    /** Appointments of the logged-in doctor on a date, optionally filtered by patient name. */
    public Map<String, Object> getAppointments(String patientName, LocalDate date, String token) {
        Map<String, Object> result = new HashMap<>();
        Doctor doctor = doctorRepository.findByEmail(tokenService.extractIdentifier(token));
        if (doctor == null) {
            result.put("appointments", List.of());
            return result;
        }
        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = date.atTime(LocalTime.MAX);
        List<Appointment> list = (patientName == null || patientName.isBlank())
                ? appointmentRepository.findByDoctor_IdAndAppointmentTimeBetween(doctor.getId(), start, end)
                : appointmentRepository.findByDoctorAndPatientNameAndDay(doctor.getId(), patientName, start, end);
        result.put("appointments", list);
        return result;
    }

    private boolean isSlotFree(Doctor doctor, LocalDateTime time, Long excludeAppointmentId) {
        if (time == null) {
            return false;
        }
        String start = time.toLocalTime().format(HH_MM);
        boolean offered = doctor.getAvailableTimes().stream().anyMatch(slot -> slot.startsWith(start));
        if (!offered) {
            return false;
        }
        return appointmentRepository
                .findByDoctor_IdAndAppointmentTimeBetween(doctor.getId(), time.minusMinutes(59), time.plusMinutes(59))
                .stream()
                .allMatch(a -> a.getId().equals(excludeAppointmentId));
    }
}
