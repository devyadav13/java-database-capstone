# Architecture Design Document

## 1. Overview
Smart Clinic Management System is a Spring Boot application with a browser frontend. Relational data (admins, doctors, patients, appointments) lives in **MySQL**; flexible prescription documents live in **MongoDB**. Access is controlled with **JWT** tokens and three roles: Admin, Doctor, Patient.

## 2. Architecture
```
Browser (HTML / CSS / JS)
   |  fetch() JSON                     page requests
   v                                       v
REST controllers (@RestController)   MVC controllers (@Controller + Thymeleaf)
   \____________________  ________________/
                        \/
                 Service layer  (TokenService, DoctorService, PatientService,
                                 AppointmentService, PrescriptionService, CommonService)
                        |
        +---------------+----------------+
        v                                v
JPA repositories -> MySQL          Mongo repository -> MongoDB
(admin, doctor, patient,           (prescriptions)
 appointment, doctor_available_times)
```

- **MVC part:** `/adminDashboard/{token}` and `/doctorDashboard/{token}` are Thymeleaf pages, rendered only if the token is valid for that role (otherwise redirect to `/`).
- **REST part:** all other features are JSON endpoints under `/doctor`, `/patient`, `/appointments`, `/prescription`, `/admin`.
- **Static frontend:** `index.html` (landing and login) and `pages/patientDashboard.html`, plus shared JS modules in `static/js`.

## 3. Request flow (booking an appointment)
1. Patient logs in: `POST /patient/login` returns a JWT.
2. Patient picks a date: `GET /doctor/availability/patient/{doctorId}/{date}/{token}` returns free slots.
3. Patient confirms: `POST /appointments/{token}`. The patient is taken from the token (never from the request body), the slot is re-checked, and the appointment is saved.

## 4. Security
- Passwords are hashed with BCrypt (`spring-security-crypto`).
- JWT (HS256) subject = email (username for admins). Secret comes from the `JWT_SECRET` environment variable.
- Every protected endpoint validates the token **and** the role.
- Patients can only read, change or cancel their own appointments; doctors can only prescribe for and complete their own appointments.
- Password fields are `WRITE_ONLY` in JSON, so they are never returned by the API.

## 5. Role permissions
| Capability | Admin | Doctor | Patient |
|---|:-:|:-:|:-:|
| Log in | yes | yes | yes |
| Add / delete doctors | yes | no | no |
| View doctor list and search | yes | yes | yes (public) |
| Sign up | no | no | yes |
| Book / move / cancel own appointment | no | no | yes |
| View own appointments | no | yes (as doctor) | yes (as patient) |
| Mark appointment completed | no | yes (own) | no |
| Write prescriptions | no | yes (own) | no |

## 6. Deployment
- `Dockerfile`: multi-stage (Maven build, then slim JRE runtime, non-root user, port 8080).
- `docker-compose.yml`: `app` + `mysql` (initialised from `sql/`) + `mongo`.
- `.github/workflows/ci.yml`: compile, test, package, Docker build, Trivy security scan on every push and pull request.
