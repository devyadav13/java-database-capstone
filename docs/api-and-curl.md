# API Reference and curl Commands

Base URL: `http://localhost:8080`. All sample accounts use the password `Password@123`.

| Role | Login |
|---|---|
| Admin | `admin` |
| Doctor | `sarah.smith@clinic.com` |
| Patient | `john.doe@example.com` |

## Get tokens
```bash
ADMIN_TOKEN=$(curl -s -X POST localhost:8080/admin/login -H 'Content-Type: application/json' \
  -d '{"identifier":"admin","password":"Password@123"}' | sed 's/.*"token":"\([^"]*\)".*/\1/')

DOCTOR_TOKEN=$(curl -s -X POST localhost:8080/doctor/login -H 'Content-Type: application/json' \
  -d '{"identifier":"sarah.smith@clinic.com","password":"Password@123"}' | sed 's/.*"token":"\([^"]*\)".*/\1/')

PATIENT_TOKEN=$(curl -s -X POST localhost:8080/patient/login -H 'Content-Type: application/json' \
  -d '{"identifier":"john.doe@example.com","password":"Password@123"}' | sed 's/.*"token":"\([^"]*\)".*/\1/')
```

## Submission commands
```bash
# Q24 - GET all doctors
curl -s localhost:8080/doctor | python3 -m json.tool

# Q25 - all appointments booked by a patient (patient id 1 = John Doe)
curl -s localhost:8080/patient/1/$PATIENT_TOKEN | python3 -m json.tool

# Q26 - doctors for a speciality and time (Cardiologist, morning)
curl -s localhost:8080/doctor/filter/null/AM/Cardiologist | python3 -m json.tool
```

## More examples
```bash
# Availability of doctor 1 on a date
curl -s localhost:8080/doctor/availability/patient/1/2026-09-25/$PATIENT_TOKEN

# Admin adds a doctor
curl -s -X POST localhost:8080/doctor/$ADMIN_TOKEN -H 'Content-Type: application/json' \
  -d '{"name":"Dr. Test Doctor","specialty":"General","email":"test.doc@clinic.com","password":"Secret123","phone":"9000000000","availableTimes":["09:00-10:00","10:00-11:00"]}'

# Patient books an appointment (patient comes from the token)
curl -s -X POST localhost:8080/appointments/$PATIENT_TOKEN -H 'Content-Type: application/json' \
  -d '{"doctor":{"id":1},"appointmentTime":"2026-09-25T10:00:00"}'

# Doctor lists appointments for a date (use "null" to skip the patient-name filter)
curl -s localhost:8080/appointments/2026-09-19/null/$DOCTOR_TOKEN

# Doctor saves a prescription (appointment 1 belongs to doctor 1)
curl -s -X POST localhost:8080/prescription/$DOCTOR_TOKEN -H 'Content-Type: application/json' \
  -d '{"appointmentId":1,"patientName":"John Doe","medication":"Amoxicillin","dosage":"500 mg three times daily","doctorNotes":"After meals"}'
```

## Endpoint summary
| Method | Path | Role | Purpose |
|---|---|---|---|
| POST | `/admin/login` | - | admin login |
| POST | `/doctor/login` | - | doctor login |
| GET | `/doctor` | public | list doctors |
| GET | `/doctor/filter/{name}/{time}/{speciality}` | public | search (`null` skips a filter, time = AM/PM) |
| GET | `/doctor/availability/{user}/{doctorId}/{date}/{token}` | patient, doctor | free slots |
| POST | `/doctor/{token}` | admin | add doctor |
| PUT | `/doctor/{token}` | admin | update doctor |
| DELETE | `/doctor/{id}/{token}` | admin | delete doctor |
| POST | `/patient` | - | sign up |
| POST | `/patient/login` | - | patient login |
| GET | `/patient/{token}` | patient | own details |
| GET | `/patient/{id}/{token}` | patient | own appointments |
| GET | `/patient/filter/{condition}/{name}/{id}/{token}` | patient | filter appointments (`past`/`future`, doctor name) |
| GET | `/appointments/{date}/{patientName}/{token}` | doctor | schedule for a date |
| POST | `/appointments/{token}` | patient | book |
| PUT | `/appointments/{token}` | patient | move |
| DELETE | `/appointments/{id}/{token}` | patient | cancel |
| PUT | `/appointments/complete/{id}/{token}` | doctor | mark completed |
| POST | `/prescription/{token}` | doctor | save prescription (MongoDB) |
| GET | `/prescription/{appointmentId}/{token}` | doctor | view prescription |
