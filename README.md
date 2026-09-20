# Smart Clinic Management System

Spring Boot + MySQL + MongoDB clinic portal for admins, doctors and patients, with JWT auth, Docker and GitHub Actions CI.

## Run it

**With Docker (recommended)**
```bash
docker compose up --build
```
Open http://localhost:8080. MySQL is initialised from `sql/` (schema, sample data, stored procedures).

**Without Docker** (needs Java 17, Maven, MySQL 8 and MongoDB running locally)
```bash
mysql -u root -p < sql/schema.sql
mysql -u root -p < sql/sample-data.sql
mysql -u root -p < sql/stored-procedures.sql
mvn spring-boot:run
```
Override connections with `MYSQL_URL`, `MYSQL_USER`, `MYSQL_PASSWORD`, `MONGO_URI` and `JWT_SECRET`.

## Sample logins (password for all: `Password@123`)
| Role | Login |
|---|---|
| Admin | `admin` |
| Doctor | `sarah.smith@clinic.com` |
| Patient | `john.doe@example.com` |

## Project layout
```
src/main/java/com/smartclinic/   model, repository, service, controller, dto, config
src/main/resources/static/       index.html, pages/, css/, js/
src/main/resources/templates/    Thymeleaf admin and doctor dashboards
sql/                             schema, sample data, stored procedures
docs/                            architecture, user stories, schema design, API + curl
Dockerfile, docker-compose.yml   containerisation
.github/workflows/ci.yml         CI pipeline
```

## Documentation
- [Architecture](docs/architecture.md)
- [User stories](docs/user-stories.md)
- [Schema design](docs/schema-design.md)
- [API and curl commands](docs/api-and-curl.md)

## Final submission map
| Q | Where to find it |
|---|---|
| 1 | GitHub Issues - create from `docs/user-stories.md` |
| 2 | `docs/schema-design.md` |
| 3 | `src/main/java/com/smartclinic/model/Doctor.java` |
| 4 | `.../model/Appointment.java` |
| 5 | `.../controller/DoctorController.java` |
| 6 | `.../service/AppointmentService.java` |
| 7 | `.../controller/PrescriptionController.java` |
| 8 | `.../repository/PatientRepository.java` |
| 9 | `.../service/TokenService.java` |
| 10 | `.../service/DoctorService.java` |
| 11 | `Dockerfile` |
| 12 | `.github/workflows/ci.yml` |
| 13-15 | Screenshots of the three login modals at `/` (click each role button) |
| 16 | Admin dashboard, "Add doctor" form filled in |
| 17 | `/pages/patientDashboard.html`, search a doctor by name |
| 18 | Doctor dashboard appointment list |
| 19-23 | MySQL: `SHOW TABLES;`, `SELECT * FROM patient LIMIT 5;`, `CALL GetDailyAppointmentReportByDoctor(CURDATE());`, `CALL GetDoctorWithMostPatientsByMonth(MONTH(CURDATE()), YEAR(CURDATE()));`, `CALL GetDoctorWithMostPatientsByYear(YEAR(CURDATE()));` |
| 24-26 | curl commands in `docs/api-and-curl.md` |

Run the SQL commands with:
`docker compose exec mysql mysql -uroot -proot cms -e "SHOW TABLES;"`

## Push to GitHub
```bash
git init && git add . && git commit -m "Smart Clinic Management System"
git branch -M main
git remote add origin https://github.com/<you>/smart-clinic.git
git push -u origin main
```
The repository must be **public** so the submission links work.
