USE cms;

DROP PROCEDURE IF EXISTS GetDailyAppointmentReportByDoctor;
DROP PROCEDURE IF EXISTS GetDoctorWithMostPatientsByMonth;
DROP PROCEDURE IF EXISTS GetDoctorWithMostPatientsByYear;

DELIMITER $$

-- All appointments on a given date, grouped by doctor
CREATE PROCEDURE GetDailyAppointmentReportByDoctor(IN report_date DATE)
BEGIN
    SELECT d.name             AS doctor_name,
           a.appointment_time,
           a.status,
           p.name             AS patient_name,
           p.phone            AS patient_phone
    FROM appointment a
    JOIN doctor  d ON a.doctor_id  = d.id
    JOIN patient p ON a.patient_id = p.id
    WHERE DATE(a.appointment_time) = report_date
    ORDER BY d.name, a.appointment_time;
END$$

-- Doctor who saw the most distinct patients in a month
CREATE PROCEDURE GetDoctorWithMostPatientsByMonth(IN input_month INT, IN input_year INT)
BEGIN
    SELECT d.id, d.name AS doctor_name, d.specialty,
           COUNT(DISTINCT a.patient_id) AS patients_seen
    FROM appointment a
    JOIN doctor d ON a.doctor_id = d.id
    WHERE MONTH(a.appointment_time) = input_month
      AND YEAR(a.appointment_time)  = input_year
    GROUP BY d.id, d.name, d.specialty
    ORDER BY patients_seen DESC
    LIMIT 1;
END$$

-- Doctor who saw the most distinct patients in a year
CREATE PROCEDURE GetDoctorWithMostPatientsByYear(IN input_year INT)
BEGIN
    SELECT d.id, d.name AS doctor_name, d.specialty,
           COUNT(DISTINCT a.patient_id) AS patients_seen
    FROM appointment a
    JOIN doctor d ON a.doctor_id = d.id
    WHERE YEAR(a.appointment_time) = input_year
    GROUP BY d.id, d.name, d.specialty
    ORDER BY patients_seen DESC
    LIMIT 1;
END$$

DELIMITER ;
