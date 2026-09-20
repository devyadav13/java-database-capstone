-- Smart Clinic Management System - MySQL schema
CREATE DATABASE IF NOT EXISTS cms;
USE cms;

CREATE TABLE IF NOT EXISTS admin (
    id       BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL
);

CREATE TABLE IF NOT EXISTS doctor (
    id        BIGINT AUTO_INCREMENT PRIMARY KEY,
    name      VARCHAR(100) NOT NULL,
    specialty VARCHAR(50)  NOT NULL,
    email     VARCHAR(255) NOT NULL UNIQUE,
    password  VARCHAR(255) NOT NULL,
    phone     VARCHAR(20)  NOT NULL
);

CREATE TABLE IF NOT EXISTS doctor_available_times (
    doctor_id       BIGINT      NOT NULL,
    available_times VARCHAR(20) NOT NULL,
    CONSTRAINT fk_available_times_doctor FOREIGN KEY (doctor_id) REFERENCES doctor (id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS patient (
    id       BIGINT AUTO_INCREMENT PRIMARY KEY,
    name     VARCHAR(100) NOT NULL,
    email    VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    phone    VARCHAR(20)  NOT NULL,
    address  VARCHAR(255) NOT NULL
);

CREATE TABLE IF NOT EXISTS appointment (
    id               BIGINT AUTO_INCREMENT PRIMARY KEY,
    doctor_id        BIGINT   NOT NULL,
    patient_id       BIGINT   NOT NULL,
    appointment_time DATETIME NOT NULL,
    status           INT      NOT NULL DEFAULT 0,  -- 0 = scheduled, 1 = completed
    CONSTRAINT fk_appointment_doctor  FOREIGN KEY (doctor_id)  REFERENCES doctor (id),
    CONSTRAINT fk_appointment_patient FOREIGN KEY (patient_id) REFERENCES patient (id),
    INDEX idx_appointment_doctor_time (doctor_id, appointment_time),
    INDEX idx_appointment_patient (patient_id)
);
