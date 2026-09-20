# Database Schema Design

## MySQL (relational data)

### admin
| Column | Type | Constraints |
|---|---|---|
| id | BIGINT | PK, AUTO_INCREMENT |
| username | VARCHAR(255) | NOT NULL, UNIQUE |
| password | VARCHAR(255) | NOT NULL (BCrypt hash) |

### doctor
| Column | Type | Constraints |
|---|---|---|
| id | BIGINT | PK, AUTO_INCREMENT |
| name | VARCHAR(100) | NOT NULL |
| specialty | VARCHAR(50) | NOT NULL |
| email | VARCHAR(255) | NOT NULL, UNIQUE |
| password | VARCHAR(255) | NOT NULL (BCrypt hash) |
| phone | VARCHAR(20) | NOT NULL |

### doctor_available_times
| Column | Type | Constraints |
|---|---|---|
| doctor_id | BIGINT | NOT NULL, **FK -> doctor.id** (ON DELETE CASCADE) |
| available_times | VARCHAR(20) | NOT NULL, e.g. `09:00-10:00` |

### patient
| Column | Type | Constraints |
|---|---|---|
| id | BIGINT | PK, AUTO_INCREMENT |
| name | VARCHAR(100) | NOT NULL |
| email | VARCHAR(255) | NOT NULL, UNIQUE |
| password | VARCHAR(255) | NOT NULL (BCrypt hash) |
| phone | VARCHAR(20) | NOT NULL |
| address | VARCHAR(255) | NOT NULL |

### appointment
| Column | Type | Constraints |
|---|---|---|
| id | BIGINT | PK, AUTO_INCREMENT |
| doctor_id | BIGINT | NOT NULL, **FK -> doctor.id** |
| patient_id | BIGINT | NOT NULL, **FK -> patient.id** |
| appointment_time | DATETIME | NOT NULL |
| status | INT | NOT NULL, DEFAULT 0 (0 = scheduled, 1 = completed) |

Indexes: `(doctor_id, appointment_time)` for the daily schedule; `(patient_id)` for patient history.

### Relationships
```
doctor 1 ---- * doctor_available_times
doctor 1 ---- * appointment * ---- 1 patient
```

## MongoDB (flexible data)

Collection: `prescriptions`

```json
{
  "_id": "ObjectId('66f1...')",
  "appointmentId": 12,
  "patientName": "John Doe",
  "medication": "Amoxicillin",
  "dosage": "500 mg, three times daily for 7 days",
  "doctorNotes": "Take after meals. Return if fever persists."
}
```

Why MongoDB here: prescriptions vary in shape (multiple drugs, notes, refill info later) and are read by appointment, so a document per prescription avoids many sparse relational columns. `appointmentId` links back to the MySQL `appointment.id`.

## Stored procedures (`sql/stored-procedures.sql`)
| Procedure | Input | Result |
|---|---|---|
| `GetDailyAppointmentReportByDoctor` | date | every appointment that day with doctor and patient |
| `GetDoctorWithMostPatientsByMonth` | month, year | doctor with most distinct patients |
| `GetDoctorWithMostPatientsByYear` | year | doctor with most distinct patients |
