package com.smartclinic.repository;

import com.smartclinic.model.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    List<Appointment> findByDoctor_IdAndAppointmentTimeBetween(Long doctorId, LocalDateTime start, LocalDateTime end);

    @Query("SELECT a FROM Appointment a WHERE a.doctor.id = :doctorId "
            + "AND LOWER(a.patient.name) LIKE LOWER(CONCAT('%', :patientName, '%')) "
            + "AND a.appointmentTime BETWEEN :start AND :end")
    List<Appointment> findByDoctorAndPatientNameAndDay(@Param("doctorId") Long doctorId,
                                                       @Param("patientName") String patientName,
                                                       @Param("start") LocalDateTime start,
                                                       @Param("end") LocalDateTime end);

    List<Appointment> findByPatient_IdOrderByAppointmentTimeAsc(Long patientId);

    List<Appointment> findByPatient_IdAndStatusOrderByAppointmentTimeAsc(Long patientId, int status);

    @Query("SELECT a FROM Appointment a WHERE a.patient.id = :patientId "
            + "AND LOWER(a.doctor.name) LIKE LOWER(CONCAT('%', :doctorName, '%')) "
            + "ORDER BY a.appointmentTime ASC")
    List<Appointment> filterByDoctorName(@Param("patientId") Long patientId, @Param("doctorName") String doctorName);

    @Query("SELECT a FROM Appointment a WHERE a.patient.id = :patientId AND a.status = :status "
            + "AND LOWER(a.doctor.name) LIKE LOWER(CONCAT('%', :doctorName, '%')) "
            + "ORDER BY a.appointmentTime ASC")
    List<Appointment> filterByDoctorNameAndStatus(@Param("patientId") Long patientId,
                                                  @Param("doctorName") String doctorName,
                                                  @Param("status") int status);

    @Transactional
    void deleteAllByDoctor_Id(Long doctorId);

    /** Direct update so the @Future check on appointmentTime does not block completing past appointments. */
    @Transactional
    @Modifying
    @Query("UPDATE Appointment a SET a.status = 1 WHERE a.id = :id")
    int markCompleted(@Param("id") Long id);
}
