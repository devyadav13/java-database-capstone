package com.smartclinic.service;

import com.smartclinic.dto.Login;
import com.smartclinic.model.Appointment;
import com.smartclinic.model.Doctor;
import com.smartclinic.repository.AppointmentRepository;
import com.smartclinic.repository.DoctorRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class DoctorService {

    private static final DateTimeFormatter HH_MM = DateTimeFormatter.ofPattern("HH:mm");

    private final DoctorRepository doctorRepository;
    private final AppointmentRepository appointmentRepository;
    private final TokenService tokenService;
    private final PasswordEncoder passwordEncoder;

    public DoctorService(DoctorRepository doctorRepository,
                         AppointmentRepository appointmentRepository,
                         TokenService tokenService,
                         PasswordEncoder passwordEncoder) {
        this.doctorRepository = doctorRepository;
        this.appointmentRepository = appointmentRepository;
        this.tokenService = tokenService;
        this.passwordEncoder = passwordEncoder;
    }

    /** Returns the doctor's slots for a date, minus the slots that are already booked. */
    public List<String> getDoctorAvailability(Long doctorId, LocalDate date) {
        Optional<Doctor> doctor = doctorRepository.findById(doctorId);
        if (doctor.isEmpty()) {
            return List.of();
        }
        List<Appointment> booked = appointmentRepository.findByDoctor_IdAndAppointmentTimeBetween(
                doctorId, date.atStartOfDay(), date.atTime(LocalTime.MAX));
        Set<String> bookedStarts = booked.stream()
                .map(a -> a.getAppointmentTime().toLocalTime().format(HH_MM))
                .collect(Collectors.toSet());
        return doctor.get().getAvailableTimes().stream()
                .filter(slot -> !bookedStarts.contains(slotStart(slot)))
                .sorted()
                .collect(Collectors.toList());
    }

    /** @return 1 saved, -1 email already exists, 0 internal error */
    public int saveDoctor(Doctor doctor) {
        try {
            if (doctorRepository.findByEmail(doctor.getEmail()) != null) {
                return -1;
            }
            doctor.setId(null);
            doctor.setPassword(passwordEncoder.encode(doctor.getPassword()));
            doctorRepository.save(doctor);
            return 1;
        } catch (Exception e) {
            return 0;
        }
    }

    /** @return 1 updated, -1 not found, 0 internal error */
    public int updateDoctor(Doctor updated) {
        try {
            Optional<Doctor> existing = doctorRepository.findById(updated.getId());
            if (existing.isEmpty()) {
                return -1;
            }
            Doctor doctor = existing.get();
            doctor.setName(updated.getName());
            doctor.setSpecialty(updated.getSpecialty());
            doctor.setPhone(updated.getPhone());
            doctor.setAvailableTimes(updated.getAvailableTimes());
            if (updated.getPassword() != null && !updated.getPassword().isBlank()) {
                doctor.setPassword(passwordEncoder.encode(updated.getPassword()));
            }
            doctorRepository.save(doctor);
            return 1;
        } catch (Exception e) {
            return 0;
        }
    }

    public List<Doctor> getDoctors() {
        return doctorRepository.findAll();
    }

    /** @return 1 deleted, -1 not found, 0 internal error */
    public int deleteDoctor(Long id) {
        try {
            if (!doctorRepository.existsById(id)) {
                return -1;
            }
            appointmentRepository.deleteAllByDoctor_Id(id);
            doctorRepository.deleteById(id);
            return 1;
        } catch (Exception e) {
            return 0;
        }
    }

    /** Validates doctor credentials and returns a token or an error message. */
    public ResponseEntity<Map<String, String>> validateDoctor(Login login) {
        try {
            Doctor doctor = doctorRepository.findByEmail(login.getIdentifier());
            if (doctor == null || !passwordEncoder.matches(login.getPassword(), doctor.getPassword())) {
                return CommonService.message(HttpStatus.UNAUTHORIZED, "Invalid email or password");
            }
            Map<String, String> body = new HashMap<>();
            body.put("token", tokenService.generateToken(doctor.getEmail()));
            return ResponseEntity.ok(body);
        } catch (Exception e) {
            return CommonService.message(HttpStatus.INTERNAL_SERVER_ERROR, "Login failed. Please try again.");
        }
    }

    /** Filters by any combination of name, specialty and AM/PM. Null or blank means "no filter". */
    public List<Doctor> filterDoctors(String name, String specialty, String amOrPm) {
        boolean hasName = name != null && !name.isBlank();
        boolean hasSpecialty = specialty != null && !specialty.isBlank();

        List<Doctor> doctors;
        if (hasName && hasSpecialty) {
            doctors = doctorRepository.findByNameContainingIgnoreCaseAndSpecialtyIgnoreCase(name, specialty);
        } else if (hasName) {
            doctors = doctorRepository.findByNameContainingIgnoreCase(name);
        } else if (hasSpecialty) {
            doctors = doctorRepository.findBySpecialtyIgnoreCase(specialty);
        } else {
            doctors = doctorRepository.findAll();
        }
        return filterByTime(doctors, amOrPm);
    }

    private List<Doctor> filterByTime(List<Doctor> doctors, String amOrPm) {
        if (amOrPm == null || amOrPm.isBlank()) {
            return doctors;
        }
        boolean morning = amOrPm.equalsIgnoreCase("AM");
        boolean afternoon = amOrPm.equalsIgnoreCase("PM");
        if (!morning && !afternoon) {
            return doctors;
        }
        return doctors.stream()
                .filter(d -> d.getAvailableTimes().stream().anyMatch(slot -> {
                    int hour = Integer.parseInt(slotStart(slot).substring(0, 2));
                    return morning ? hour < 12 : hour >= 12;
                }))
                .collect(Collectors.toList());
    }

    private static String slotStart(String slot) {
        return slot.split("-")[0].trim();
    }
}
