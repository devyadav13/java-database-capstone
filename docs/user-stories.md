# User Stories

Copy each story into a GitHub Issue (title = the story headline, labels: `admin`, `doctor` or `patient`). The acceptance criteria go in the issue body.

## Admin

**A1 - Log in**
As an **admin**, I want to log in with my username and password, so that I can manage the clinic securely.
*Acceptance:* valid credentials open the admin dashboard; invalid credentials show an error; the dashboard cannot be opened without a valid token.

**A2 - Add a doctor**
As an **admin**, I want to add a doctor with name, specialty, email, phone, password and available slots, so that patients can book them.
*Acceptance:* duplicate emails are rejected; the new doctor appears in the list immediately.

**A3 - Delete a doctor**
As an **admin**, I want to delete a doctor's profile, so that former staff are no longer bookable.
*Acceptance:* the doctor and their appointments are removed; a confirmation is required.

**A4 - Search doctors**
As an **admin**, I want to filter doctors by name, specialty and time of day, so that I can find a profile quickly.

**A5 - Usage report**
As an **admin**, I want to run stored procedures that report daily appointments per doctor and the busiest doctor by month and year, so that I can track clinic usage.

## Doctor

**D1 - Log in**
As a **doctor**, I want to log in with my email and password, so that I can see my schedule.

**D2 - View appointments**
As a **doctor**, I want to see my appointments for a chosen date, so that I can prepare for the day.
*Acceptance:* list shows time, patient, phone and status; I can filter by patient name.

**D3 - Complete an appointment**
As a **doctor**, I want to mark an appointment as completed, so that records stay accurate.

**D4 - Write a prescription**
As a **doctor**, I want to save medication, dosage and notes for a patient's appointment, so that the patient has a record of their treatment.
*Acceptance:* only for my own appointments; one prescription per appointment.

**D5 - Manage availability**
As a **doctor**, I want my available time slots to be respected by the booking system, so that patients never double-book me.

## Patient

**P1 - Sign up**
As a **patient**, I want to create an account with my name, email, phone, address and password, so that I can book appointments.
*Acceptance:* duplicate email or phone is rejected; phone must be 10 digits.

**P2 - Log in**
As a **patient**, I want to log in, so that I can see and manage my bookings.

**P3 - Find a doctor**
As a **patient**, I want to search doctors by name, specialty and morning/afternoon, so that I can pick the right one, even before logging in.

**P4 - Book an appointment**
As a **patient**, I want to choose a date and an open slot, so that I get a confirmed appointment.
*Acceptance:* booked slots are no longer offered; past times are rejected.

**P5 - View my appointments**
As a **patient**, I want to see upcoming and completed appointments, so that I know my schedule.

**P6 - Cancel an appointment**
As a **patient**, I want to cancel an upcoming appointment, so that the slot is free for others.
*Acceptance:* I can only cancel my own appointments.
