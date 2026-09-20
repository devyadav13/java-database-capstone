package com.smartclinic.repository;

import com.smartclinic.model.Patient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PatientRepository extends JpaRepository<Patient, Long> {

    /** Derived query: find a patient by email. */
    Patient findByEmail(String email);

    /** Custom query: find a patient by email OR phone number. */
    @Query("SELECT p FROM Patient p WHERE p.email = :email OR p.phone = :phone")
    Patient findByEmailOrPhone(@Param("email") String email, @Param("phone") String phone);
}
