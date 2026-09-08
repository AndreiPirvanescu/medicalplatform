-- ============================================================
-- Insertion order respects FK dependencies:
-- roles -> users -> user_roles -> medical_units -> doctors ->
-- doctor_specializations -> patients -> medical_records ->
-- schedules -> appointments -> medications -> prescriptions ->
-- prescription_medications
-- ============================================================

-- ---------------------------
-- roles
-- ---------------------------
INSERT INTO roles (id, name) VALUES
                                 (1, 'ROLE_ADMIN'),
                                 (2, 'ROLE_DOCTOR'),
                                 (3, 'ROLE_PATIENT'),
                                 (4, 'ROLE_MANAGER');

-- ---------------------------
-- users
-- (1 admin, 5 doctors, 5 patients, 4 unit managers)
-- ---------------------------
INSERT INTO users (id, email, password, first_name, last_name, phone, account_non_expired, account_non_locked, credentials_non_expired, enabled) VALUES
                                                                                                                                                     (1,  'admin@medplatform.com',       '$2a$10$abcdefghijklmnopqrstuv', 'Andrew',    'Palmer',    '0721000001', true, true, true, true),
                                                                                                                                                     (2,  'john.davies@medplatform.com', '$2a$10$abcdefghijklmnopqrstuv', 'John',      'Davies',    '0721000002', true, true, true, true),
                                                                                                                                                     (3,  'mary.evans@medplatform.com',  '$2a$10$abcdefghijklmnopqrstuv', 'Mary',      'Evans',     '0721000003', true, true, true, true),
                                                                                                                                                     (4,  'robert.stone@medplatform.com','$2a$10$abcdefghijklmnopqrstuv', 'Robert',    'Stone',     '0721000004', true, true, true, true),
                                                                                                                                                     (5,  'ellen.hughes@medplatform.com','$2a$10$abcdefghijklmnopqrstuv', 'Ellen',     'Hughes',    '0721000005', true, true, true, true),
                                                                                                                                                     (6,  'chris.moore@medplatform.com', '$2a$10$abcdefghijklmnopqrstuv', 'Christian', 'Moore',     '0721000006', true, true, true, true),
                                                                                                                                                     (7,  'alex.parker@medplatform.com', '$2a$10$abcdefghijklmnopqrstuv', 'Alex',      'Parker',    '0722000007', true, true, true, true),
                                                                                                                                                     (8,  'george.cole@medplatform.com', '$2a$10$abcdefghijklmnopqrstuv', 'George',    'Cole',      '0722000008', true, true, true, true),
                                                                                                                                                     (9,  'diana.stevens@medplatform.com','$2a$10$abcdefghijklmnopqrstuv','Diana',     'Stevens',   '0722000009', true, true, true, true),
                                                                                                                                                     (10, 'michael.freeman@medplatform.com','$2a$10$abcdefghijklmnopqrstuv','Michael', 'Freeman',   '0722000010', true, true, true, true),
                                                                                                                                                     (11, 'anna.george@medplatform.com', '$2a$10$abcdefghijklmnopqrstuv', 'Anna',      'George',    '0722000011', true, true, true, true),
                                                                                                                                                     (12, 'brian.manager@medplatform.com','$2a$10$abcdefghijklmnopqrstuv','Brian',     'Nash',      '0723000012', true, true, true, true),
                                                                                                                                                     (13, 'susan.manager@medplatform.com','$2a$10$abcdefghijklmnopqrstuv','Susan',     'Reed',      '0723000013', true, true, true, true),
                                                                                                                                                     (14, 'victor.manager@medplatform.com','$2a$10$abcdefghijklmnopqrstuv','Victor',   'Price',     '0723000014', true, true, true, true),
                                                                                                                                                     (15, 'laura.manager@medplatform.com','$2a$10$abcdefghijklmnopqrstuv','Laura',     'Dobson',    '0723000015', true, true, true, true),
                                                                                                                                                     (16, 'levin.manager@medplatform.com','$2a$10$abcdefghijklmnopqrstuv','Levin',     'Johnson',    '0723000016', true, true, true, true),
                                                                                                                                                     (17, 'dora.manager@medplatform.com','$2a$10$abcdefghijklmnopqrstuv','Dora',     'Smith',    '0723000017', true, true, true, true);

-- ---------------------------
-- user_roles
-- ---------------------------
INSERT INTO user_roles (user_id, role_id) VALUES
                                              (1, 1),
                                              (2, 2), (3, 2), (4, 2), (5, 2), (6, 2),
                                              (7, 3), (8, 3), (9, 3), (10, 3), (11, 3),
                                              (12, 4), (13, 4), (14, 4), (15, 4);

-- ---------------------------
-- medical_units
-- ---------------------------
INSERT INTO medical_units (id, name, email, phone, address, manager_id) VALUES
                                                                            (1, 'Central City Hospital', 'contact@centralcity-hospital.com', '0213172120', '19-21 Main Street, Springfield', 12),
                                                                            (2, 'Northgate County Hospital', 'contact@northgate-hospital.com', '0264597852', '3-5 Clinic Avenue, Riverton', 13),
                                                                            (3, 'Wellness Medical Clinic', 'contact@wellnessclinic.com', '0356123456', '10 Revolution Boulevard, Fairview', 14),
                                                                            (4, 'Eastview Municipal Hospital', 'contact@eastview-hospital.com', '0232267070', '1 Independence Street, Millbrook', 15);

-- ---------------------------
-- doctors
-- ---------------------------
INSERT INTO doctors (id, user_id, medical_unit_id, license_number, bio) VALUES
                                                                            (1, 2, 1, 'LIC-US-10021', 'General surgeon with over 15 years of clinical experience.'),
                                                                            (2, 3, 2, 'LIC-US-10034', 'Cardiologist with a focus on arrhythmia and heart failure management.'),
                                                                            (3, 4, 3, 'LIC-US-10045', 'Orthopedic specialist in sports trauma and rehabilitation.'),
                                                                            (4, 5, 4, 'LIC-US-10058', 'Cardiovascular surgeon collaborating with several university hospitals.'),
                                                                            (5, 6, 4, 'LIC-US-10067', 'Pediatric orthopedist specialized in congenital limb conditions.');

-- ---------------------------
-- doctor_specializations (element collection)
-- ---------------------------
INSERT INTO doctor_specializations (doctor_id, specializations) VALUES
                                                                    (1, 'CHIRURGIE'),
                                                                    (2, 'CARDIOLOGIE'),
                                                                    (2, 'CHIRURGIE'),
                                                                    (3, 'ORTOPEDIE'),
                                                                    (4, 'CARDIOLOGIE'),
                                                                    (4, 'CHIRURGIE'),
                                                                    (5, 'ORTOPEDIE');

-- ---------------------------
-- patients
-- ---------------------------
INSERT INTO patients (id, user_id, date_of_birth, blood_type) VALUES
                                                                  (1, 7,  '1990-05-14', 'A+'),
                                                                  (2, 8,  '1985-11-02', 'O-'),
                                                                  (3, 9,  '2000-01-23', 'B+'),
                                                                  (4, 10, '1975-07-30', 'AB+'),
                                                                  (5, 11, '1998-09-09', 'O+');

-- ---------------------------
-- medical_records
-- ---------------------------
INSERT INTO medical_records (id, patient_id, created_date, notes) VALUES
                                                                      (1, 1, '2023-02-10 09:00:00', 'No known allergies. Family history of hypertension.'),
                                                                      (2, 2, '2022-06-18 11:30:00', 'Minor surgical history (appendectomy, 2010).'),
                                                                      (3, 3, '2024-01-05 14:15:00', 'No chronic conditions. Vaccinations up to date.'),
                                                                      (4, 4, '2021-09-22 08:45:00', 'Type II diabetes, currently on metformin.'),
                                                                      (5, 5, '2023-11-30 16:20:00', 'Penicillin allergy. Mild bronchial asthma.');

-- ---------------------------
-- schedules
-- ---------------------------
INSERT INTO schedules (id, start_time, end_time, available, doctor_id) VALUES
                                                                           (1, '2026-09-08 09:00:00', '2026-09-08 12:00:00', true,  1),
                                                                           (2, '2026-09-08 13:00:00', '2026-09-08 16:00:00', false, 2),
                                                                           (3, '2026-09-09 08:30:00', '2026-09-09 11:30:00', true,  3),
                                                                           (4, '2026-09-09 12:00:00', '2026-09-09 15:00:00', true,  4),
                                                                           (5, '2026-09-10 10:00:00', '2026-09-10 13:00:00', false, 5);

-- ---------------------------
-- appointments
-- ---------------------------
INSERT INTO appointments (id, patient_id, doctor_id, appointment_date_time, status) VALUES
                                                                                        (1, 1, 1, '2026-09-08 09:30:00', 'COMPLETED'),
                                                                                        (2, 2, 2, '2026-09-08 13:30:00', 'CANCELLED'),
                                                                                        (3, 3, 3, '2026-09-09 09:00:00', 'COMPLETED'),
                                                                                        (4, 4, 4, '2026-09-09 12:30:00', 'SCHEDULED'),
                                                                                        (5, 5, 5, '2026-09-10 10:30:00', 'SCHEDULED');

-- ---------------------------
-- medications
-- ---------------------------
INSERT INTO medications (id, name, dosage, description) VALUES
                                                            (1, 'Paracetamol', '500mg', 'Analgesic and antipyretic used for mild-to-moderate pain and fever.'),
                                                            (2, 'Ibuprofen', '400mg', 'Nonsteroidal anti-inflammatory drug used for pain and inflammation.'),
                                                            (3, 'Amoxicillin', '875mg', 'Penicillin-class antibiotic used to treat bacterial infections.'),
                                                            (4, 'Metformin', '850mg', 'Oral antidiabetic medication used in the treatment of type II diabetes.'),
                                                            (5, 'Atorvastatin', '20mg', 'Statin used to lower LDL cholesterol levels.');

-- ---------------------------
-- prescriptions
-- ---------------------------
INSERT INTO prescriptions (id, patient_id, doctor_id, appointment_id, issue_date, notes) VALUES
                                                                                             (1, 3, 3, 3, '2026-09-09 09:20:00', 'Rest is recommended along with adherence to the prescribed regimen.'),
                                                                                             (2, 5, 5, 5, '2026-09-10 10:50:00', 'Continue antibiotic treatment for 7 days.'),
                                                                                             (3, 4, 4, 4, '2026-09-09 12:45:00', 'Weekly blood glucose monitoring.'),
                                                                                             (4, 1, 1, 1, '2026-09-08 09:45:00', 'Symptomatic treatment for post-operative pain.'),
                                                                                             (5, 2, 2, 2, '2026-09-08 13:40:00', 'Cardiology follow-up in 3 months.');

-- ---------------------------
-- prescription_medications
-- ---------------------------
INSERT INTO prescription_medications (prescription_id, medication_id) VALUES
                                                                          (1, 2),
                                                                          (1, 3),
                                                                          (2, 3),
                                                                          (3, 4),
                                                                          (3, 5),
                                                                          (4, 1),
                                                                          (5, 5);