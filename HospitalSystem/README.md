# Hospital Appointment System

A web-based hospital management system built with Java. Handles appointment booking and management for patients, doctors, and receptionists.

## What it does

- Patients can book appointments with doctors
- Doctors can view and complete their appointments
- Receptionists can see all appointments and manage them
- Simple login system with role-based access

## Project Structure

```
HospitalSystem/
├── Server.java          # HTTP server (runs on port 8080)
├── User.java            # Base user class
├── Patient.java         # Patient model
├── Doctor.java          # Doctor model
├── Receptionist.java    # Receptionist model
├── Appointment.java     # Appointment model
├── FileManager.java     # Handles file read/write
├── LoginHandler.java    # Login API
├── users.txt            # User data
├── appointments.txt     # Appointment data
└── web/                 # Frontend files
    ├── index.html
    ├── patient.html
    ├── doctor.html
    ├── receptionist.html
    └── style.css
```

## How to Run

1. Compile all Java files:
   ```
   javac *.java
   ```

2. Start the server:
   ```
   java Server
   ```

3. Open browser at `http://localhost:8080`

## Tech Used

- Java (no external dependencies)
- Built-in HttpServer
- Plain HTML/CSS frontend
- File-based storage (txt files)