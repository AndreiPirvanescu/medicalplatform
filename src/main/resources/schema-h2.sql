-- USERS
CREATE TABLE users (
                       id BIGINT AUTO_INCREMENT PRIMARY KEY,
                       email VARCHAR(100) NOT NULL UNIQUE,
                       password VARCHAR(255) NOT NULL,
                       first_name VARCHAR(255) NOT NULL,
                       last_name VARCHAR(255) NOT NULL,
                       role VARCHAR(20) NOT NULL
);

-- DOCTORS
CREATE TABLE doctors (
                         id BIGINT AUTO_INCREMENT PRIMARY KEY,
                         user_id BIGINT NOT NULL UNIQUE,
                         license_number VARCHAR(255),
                         CONSTRAINT fk_doctors_user FOREIGN KEY (user_id) REFERENCES users(id)
);

-- DOCTOR_SPECIALIZATIONS (element collection table)
CREATE TABLE doctor_specializations (
                                        doctor_id BIGINT NOT NULL,
                                        specializations VARCHAR(50) NOT NULL,
                                        CONSTRAINT fk_doctor_specializations_doctor FOREIGN KEY (doctor_id) REFERENCES doctors(id)
);

-- PATIENTS
CREATE TABLE patients (
                          id BIGINT AUTO_INCREMENT PRIMARY KEY,
                          user_id BIGINT NOT NULL UNIQUE,
                          phone_number VARCHAR(255),
                          birth_date DATE,
                          blood_type VARCHAR(255),
                          CONSTRAINT fk_patients_user FOREIGN KEY (user_id) REFERENCES users(id)
);

-- APPOINTMENTS
CREATE TABLE appointments (
                              id BIGINT AUTO_INCREMENT PRIMARY KEY,
                              patient_id BIGINT NOT NULL,
                              doctor_id BIGINT NOT NULL,
                              appointment_date_time TIMESTAMP NOT NULL,
                              status VARCHAR(20) NOT NULL,
                              CONSTRAINT fk_appointments_patient FOREIGN KEY (patient_id) REFERENCES patients(id),
                              CONSTRAINT fk_appointments_doctor FOREIGN KEY (doctor_id) REFERENCES doctors(id)
);