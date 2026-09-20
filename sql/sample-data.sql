-- Sample data. Every account below uses the password: Password@123
USE cms;

INSERT INTO admin (username, password) VALUES
('admin', '$2b$10$h/C28H33kRZ61ShFvWqsa.xhLP10SMAUb1rjfGB3il5iscqL8WASW');

INSERT INTO doctor (id, name, specialty, email, password, phone) VALUES
(1, 'Dr. Sarah Smith',   'Cardiologist', 'sarah.smith@clinic.com',   '$2b$10$h/C28H33kRZ61ShFvWqsa.xhLP10SMAUb1rjfGB3il5iscqL8WASW', '9876500001'),
(2, 'Dr. Rahul Verma',   'Dermatologist','rahul.verma@clinic.com',   '$2b$10$h/C28H33kRZ61ShFvWqsa.xhLP10SMAUb1rjfGB3il5iscqL8WASW', '9876500002'),
(3, 'Dr. Anita Desai',   'Pediatrician', 'anita.desai@clinic.com',   '$2b$10$h/C28H33kRZ61ShFvWqsa.xhLP10SMAUb1rjfGB3il5iscqL8WASW', '9876500003'),
(4, 'Dr. Michael Chen',  'Neurologist',  'michael.chen@clinic.com',  '$2b$10$h/C28H33kRZ61ShFvWqsa.xhLP10SMAUb1rjfGB3il5iscqL8WASW', '9876500004'),
(5, 'Dr. Priya Nair',    'Orthopedic',   'priya.nair@clinic.com',    '$2b$10$h/C28H33kRZ61ShFvWqsa.xhLP10SMAUb1rjfGB3il5iscqL8WASW', '9876500005'),
(6, 'Dr. James Wilson',  'Cardiologist', 'james.wilson@clinic.com',  '$2b$10$h/C28H33kRZ61ShFvWqsa.xhLP10SMAUb1rjfGB3il5iscqL8WASW', '9876500006');

INSERT INTO doctor_available_times (doctor_id, available_times) VALUES
(1,'09:00-10:00'),(1,'10:00-11:00'),(1,'11:00-12:00'),(1,'14:00-15:00'),
(2,'09:00-10:00'),(2,'10:00-11:00'),(2,'15:00-16:00'),(2,'16:00-17:00'),
(3,'09:00-10:00'),(3,'11:00-12:00'),(3,'14:00-15:00'),(3,'15:00-16:00'),
(4,'10:00-11:00'),(4,'11:00-12:00'),(4,'14:00-15:00'),(4,'16:00-17:00'),
(5,'09:00-10:00'),(5,'10:00-11:00'),(5,'14:00-15:00'),(5,'15:00-16:00'),
(6,'14:00-15:00'),(6,'15:00-16:00'),(6,'16:00-17:00');

INSERT INTO patient (id, name, email, password, phone, address) VALUES
(1, 'John Doe',       'john.doe@example.com',       '$2b$10$h/C28H33kRZ61ShFvWqsa.xhLP10SMAUb1rjfGB3il5iscqL8WASW', '9123400001', '12 Palasia Square, Indore'),
(2, 'Maria Garcia',   'maria.garcia@example.com',   '$2b$10$h/C28H33kRZ61ShFvWqsa.xhLP10SMAUb1rjfGB3il5iscqL8WASW', '9123400002', '45 Vijay Nagar, Indore'),
(3, 'Aarav Sharma',   'aarav.sharma@example.com',   '$2b$10$h/C28H33kRZ61ShFvWqsa.xhLP10SMAUb1rjfGB3il5iscqL8WASW', '9123400003', '7 Rajwada Road, Indore'),
(4, 'Emily Johnson',  'emily.johnson@example.com',  '$2b$10$h/C28H33kRZ61ShFvWqsa.xhLP10SMAUb1rjfGB3il5iscqL8WASW', '9123400004', '90 Bhawarkua Main Road, Indore'),
(5, 'Kabir Singh',    'kabir.singh@example.com',    '$2b$10$h/C28H33kRZ61ShFvWqsa.xhLP10SMAUb1rjfGB3il5iscqL8WASW', '9123400005', '23 Sapna Sangeeta, Indore'),
(6, 'Fatima Khan',    'fatima.khan@example.com',    '$2b$10$h/C28H33kRZ61ShFvWqsa.xhLP10SMAUb1rjfGB3il5iscqL8WASW', '9123400006', '5 MG Road, Indore'),
(7, 'Liam Brown',     'liam.brown@example.com',     '$2b$10$h/C28H33kRZ61ShFvWqsa.xhLP10SMAUb1rjfGB3il5iscqL8WASW', '9123400007', '61 Scheme 54, Indore'),
(8, 'Sneha Patil',    'sneha.patil@example.com',    '$2b$10$h/C28H33kRZ61ShFvWqsa.xhLP10SMAUb1rjfGB3il5iscqL8WASW', '9123400008', '18 Annapurna Road, Indore');

-- Today's appointments (used by GetDailyAppointmentReportByDoctor)
INSERT INTO appointment (doctor_id, patient_id, appointment_time, status) VALUES
(1, 1, TIMESTAMP(CURDATE(), '09:00:00'), 0),
(1, 2, TIMESTAMP(CURDATE(), '10:00:00'), 0),
(2, 3, TIMESTAMP(CURDATE(), '09:00:00'), 0),
(3, 4, TIMESTAMP(CURDATE(), '11:00:00'), 0),
(4, 5, TIMESTAMP(CURDATE(), '14:00:00'), 0);

-- Upcoming appointments
INSERT INTO appointment (doctor_id, patient_id, appointment_time, status) VALUES
(1, 3, TIMESTAMP(DATE_ADD(CURDATE(), INTERVAL 1 DAY), '11:00:00'), 0),
(2, 1, TIMESTAMP(DATE_ADD(CURDATE(), INTERVAL 2 DAY), '15:00:00'), 0),
(5, 6, TIMESTAMP(DATE_ADD(CURDATE(), INTERVAL 3 DAY), '10:00:00'), 0),
(6, 7, TIMESTAMP(DATE_ADD(CURDATE(), INTERVAL 4 DAY), '14:00:00'), 0);

-- Past appointments (this month and earlier months, for the monthly/yearly reports)
INSERT INTO appointment (doctor_id, patient_id, appointment_time, status) VALUES
(1, 1, TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL 1 DAY),  '09:00:00'), 1),
(1, 4, TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL 2 DAY),  '10:00:00'), 1),
(1, 5, TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL 3 DAY),  '11:00:00'), 1),
(2, 2, TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL 4 DAY),  '10:00:00'), 1),
(3, 6, TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL 35 DAY), '09:00:00'), 1),
(3, 7, TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL 36 DAY), '11:00:00'), 1),
(4, 8, TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL 70 DAY), '10:00:00'), 1),
(5, 1, TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL 100 DAY),'14:00:00'), 1);
