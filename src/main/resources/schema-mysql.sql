SET FOREIGN_KEY_CHECKS = 0;

DROP TABLE IF EXISTS prescription_medications;
DROP TABLE IF EXISTS prescriptions;
DROP TABLE IF EXISTS medications;
DROP TABLE IF EXISTS appointments;
DROP TABLE IF EXISTS schedules;
DROP TABLE IF EXISTS medical_records;
DROP TABLE IF EXISTS patients;
DROP TABLE IF EXISTS doctor_specializations;
DROP TABLE IF EXISTS doctors;
DROP TABLE IF EXISTS medical_units;
DROP TABLE IF EXISTS user_roles;
DROP TABLE IF EXISTS users;
DROP TABLE IF EXISTS roles;

SET FOREIGN_KEY_CHECKS = 1;

-- ---------------------------
-- roles
-- ---------------------------
CREATE TABLE roles (
                       id   BIGINT AUTO_INCREMENT PRIMARY KEY,
                       name VARCHAR(255) NOT NULL,
                       CONSTRAINT uk_roles_name UNIQUE (name)
) ENGINE=InnoDB;

-- ---------------------------
-- users
-- ---------------------------
CREATE TABLE users (
                       id                       BIGINT AUTO_INCREMENT PRIMARY KEY,
                       email                    VARCHAR(100)  NOT NULL,
                       password                 VARCHAR(255)  NOT NULL,
                       first_name               VARCHAR(255)  NOT NULL,
                       last_name                VARCHAR(255)  NOT NULL,
                       phone                    VARCHAR(255),
                       account_non_expired      BOOLEAN DEFAULT TRUE,
                       account_non_locked       BOOLEAN DEFAULT TRUE,
                       credentials_non_expired  BOOLEAN DEFAULT TRUE,
                       enabled                  BOOLEAN DEFAULT TRUE,
                       CONSTRAINT uk_users_email UNIQUE (email)
) ENGINE=InnoDB;

-- ---------------------------
-- user_roles (User <-> Role many-to-many)
-- ---------------------------
CREATE TABLE user_roles (
                            user_id BIGINT NOT NULL,
                            role_id BIGINT NOT NULL,
                            CONSTRAINT pk_user_roles PRIMARY KEY (user_id, role_id),
                            CONSTRAINT fk_user_roles_user FOREIGN KEY (user_id) REFERENCES users (id),
                            CONSTRAINT fk_user_roles_role FOREIGN KEY (role_id) REFERENCES roles (id)
) ENGINE=InnoDB;

-- ---------------------------
-- medical_units
-- ---------------------------
CREATE TABLE medical_units (
                               id         BIGINT AUTO_INCREMENT PRIMARY KEY,
                               name       VARCHAR(255),
                               email      VARCHAR(255),
                               phone      VARCHAR(255),
                               address    VARCHAR(255),
                               manager_id BIGINT,
                               CONSTRAINT uk_medical_units_manager UNIQUE (manager_id),
                               CONSTRAINT uk_medical_units_email UNIQUE (email),
                               CONSTRAINT fk_medical_units_manager FOREIGN KEY (manager_id) REFERENCES users (id)
) ENGINE=InnoDB;

-- ---------------------------
-- doctors
-- ---------------------------
CREATE TABLE doctors (
                         id               BIGINT AUTO_INCREMENT PRIMARY KEY,
                         user_id          BIGINT NOT NULL,
                         medical_unit_id  BIGINT,
                         license_number   VARCHAR(255),
                         bio              TEXT,
                         CONSTRAINT uk_doctors_user UNIQUE (user_id),
                         CONSTRAINT fk_doctors_user FOREIGN KEY (user_id) REFERENCES users (id),
                         CONSTRAINT fk_doctors_medical_unit FOREIGN KEY (medical_unit_id) REFERENCES medical_units (id)
) ENGINE=InnoDB;

-- ---------------------------
-- doctor_specializations (ElementCollection of enum Specialization)
-- ---------------------------
CREATE TABLE doctor_specializations (
                                        doctor_id       BIGINT NOT NULL,
                                        specializations VARCHAR(20),
                                        CONSTRAINT fk_doctor_specializations_doctor FOREIGN KEY (doctor_id) REFERENCES doctors (id)
) ENGINE=InnoDB;

-- ---------------------------
-- patients
-- ---------------------------
CREATE TABLE patients (
                          id            BIGINT AUTO_INCREMENT PRIMARY KEY,
                          user_id       BIGINT NOT NULL,
                          phone_number  VARCHAR(255),
                          date_of_birth    DATE,
                          blood_type    VARCHAR(255),
                          CONSTRAINT uk_patients_user UNIQUE (user_id),
                          CONSTRAINT fk_patients_user FOREIGN KEY (user_id) REFERENCES users (id)
) ENGINE=InnoDB;

-- ---------------------------
-- medical_records
-- ---------------------------
CREATE TABLE medical_records (
                                 id           BIGINT AUTO_INCREMENT PRIMARY KEY,
                                 patient_id   BIGINT NOT NULL,
                                 created_date DATETIME,
                                 notes        TEXT,
                                 CONSTRAINT uk_medical_records_patient UNIQUE (patient_id),
                                 CONSTRAINT fk_medical_records_patient FOREIGN KEY (patient_id) REFERENCES patients (id)
) ENGINE=InnoDB;

-- ---------------------------
-- schedules
-- ---------------------------
CREATE TABLE schedules (
                           id         BIGINT AUTO_INCREMENT PRIMARY KEY,
                           start_time DATETIME,
                           end_time   DATETIME,
                           available  BOOLEAN,
                           doctor_id  BIGINT,
                           CONSTRAINT fk_schedules_doctor FOREIGN KEY (doctor_id) REFERENCES doctors (id)
) ENGINE=InnoDB;

-- ---------------------------
-- appointments
-- ---------------------------
CREATE TABLE appointments (
                              id                     BIGINT AUTO_INCREMENT PRIMARY KEY,
                              patient_id             BIGINT NOT NULL,
                              doctor_id              BIGINT NOT NULL,
                              appointment_date_time  DATETIME NOT NULL,
                              status                 VARCHAR(20) NOT NULL,
                              CONSTRAINT fk_appointments_patient FOREIGN KEY (patient_id) REFERENCES patients (id),
                              CONSTRAINT fk_appointments_doctor  FOREIGN KEY (doctor_id)  REFERENCES doctors (id)
) ENGINE=InnoDB;

-- ---------------------------
-- medications
-- ---------------------------
CREATE TABLE medications (
                             id          BIGINT AUTO_INCREMENT PRIMARY KEY,
                             name        VARCHAR(255),
                             dosage      VARCHAR(255),
                             description TEXT
) ENGINE=InnoDB;

-- ---------------------------
-- prescriptions
-- ---------------------------
CREATE TABLE prescriptions (
                               id             BIGINT AUTO_INCREMENT PRIMARY KEY,
                               patient_id     BIGINT,
                               doctor_id      BIGINT,
                               appointment_id BIGINT,
                               issue_date     DATETIME,
                               notes          TEXT,
                               CONSTRAINT fk_prescriptions_patient     FOREIGN KEY (patient_id)     REFERENCES patients (id),
                               CONSTRAINT fk_prescriptions_doctor      FOREIGN KEY (doctor_id)      REFERENCES doctors (id),
                               CONSTRAINT fk_prescriptions_appointment FOREIGN KEY (appointment_id) REFERENCES appointments (id)
) ENGINE=InnoDB;

-- ---------------------------
-- prescription_medications (Prescription <-> Medication many-to-many)
-- ---------------------------
CREATE TABLE prescription_medications (
                                          prescription_id BIGINT NOT NULL,
                                          medication_id   BIGINT NOT NULL,
                                          CONSTRAINT pk_prescription_medications PRIMARY KEY (prescription_id, medication_id),
                                          CONSTRAINT fk_prescription_medications_prescription FOREIGN KEY (prescription_id) REFERENCES prescriptions (id),
                                          CONSTRAINT fk_prescription_medications_medication   FOREIGN KEY (medication_id)   REFERENCES medications (id)
) ENGINE=InnoDB;