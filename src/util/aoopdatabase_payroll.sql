-- FIXED MotorPH Database Setup Script
-- This version properly handles foreign key constraints

CREATE DATABASE IF NOT EXISTS aoopdatabase_payroll;
USE aoopdatabase_payroll;

-- Disable foreign key checks to allow table recreation
SET FOREIGN_KEY_CHECKS = 0;
SET sql_mode = '';

-- =============================================
-- Drop all tables in correct order
-- =============================================
DROP TABLE IF EXISTS payroll;
DROP TABLE IF EXISTS overtime;
DROP TABLE IF EXISTS deductions;
DROP TABLE IF EXISTS government_contributions;
DROP TABLE IF EXISTS compensation_details;
DROP TABLE IF EXISTS attendance;
DROP TABLE IF EXISTS leave_request;
DROP TABLE IF EXISTS credentials;
DROP TABLE IF EXISTS employees;

-- =============================================
-- Create employees table
-- =============================================
CREATE TABLE employees (
    employee_id INT PRIMARY KEY,
    last_name VARCHAR(50) NOT NULL,
    first_name VARCHAR(50) NOT NULL,
    birthday DATE,
    address TEXT,
    phone_number VARCHAR(20),
    sss_number VARCHAR(20),
    philhealth_number VARCHAR(20),
    tin_number VARCHAR(20),
    pagibig_number VARCHAR(20),
    status ENUM('Regular', 'Probationary') NOT NULL,
    position VARCHAR(100) NOT NULL,
    immediate_supervisor VARCHAR(100),
    basic_salary DECIMAL(10,2),
    rice_subsidy DECIMAL(8,2),
    phone_allowance DECIMAL(8,2),
    clothing_allowance DECIMAL(8,2),
    gross_semi_monthly_rate DECIMAL(10,2),
    hourly_rate DECIMAL(8,2),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- =============================================
-- Insert employee data FIRST
-- =============================================
INSERT INTO employees (employee_id, last_name, first_name, birthday, address, phone_number, sss_number, philhealth_number, tin_number, pagibig_number, status, position, immediate_supervisor, basic_salary, rice_subsidy, phone_allowance, clothing_allowance, gross_semi_monthly_rate, hourly_rate) VALUES
(10001, 'Garcia', 'Manuel III', '1983-10-11', 'Valero Carpark Building Valero Street 1227, Makati City', '966-860-270', '44-4506057-3', '820126853951', '442-605-657-000', '691295330870', 'Regular', 'Chief Executive Officer', 'NA', 90000.00, 1500.00, 2000.00, 1000.00, 45000.00, 535.71),
(10002, 'Lim', 'Antonio', '1988-06-19', 'San Antonio De Padua 2, Block 1 Lot 8 and 2, Dasmarinas, Cavite', '171-867-411', '52-2061274-9', '331735646338', '683-102-776-000', '663904995411', 'Regular', 'Chief Operating Officer', 'Garcia, Manuel III', 60000.00, 1500.00, 2000.00, 1000.00, 30000.00, 357.14),
(10003, 'Aquino', 'Bianca Sofia', '1989-08-04', 'Rm. 402 4/F Jiao Building Timog Avenue Cor. Quezon Avenue 1100, Quezon City', '966-889-370', '30-8870406-2', '177451189665', '971-711-280-000', '171519773969', 'Regular', 'Chief Finance Officer', 'Garcia, Manuel III', 60000.00, 1500.00, 2000.00, 1000.00, 30000.00, 357.14),
(10004, 'Reyes', 'Isabella', '1994-06-16', '460 Solanda Street Intramuros 1000, Manila', '786-868-477', '40-2511815-0', '341911411254', '876-809-437-000', '416946776041', 'Regular', 'Chief Marketing Officer', 'Garcia, Manuel III', 60000.00, 1500.00, 2000.00, 1000.00, 30000.00, 357.14),
(10005, 'Hernandez', 'Eduard', '1989-09-23', 'National Highway, Gingoog, Misamis Occidental', '088-861-012', '50-5577638-1', '957436191812', '031-702-374-000', '952347222457', 'Regular', 'IT Operations and Systems', 'Lim, Antonio', 52670.00, 1500.00, 1000.00, 1000.00, 26335.00, 313.51),
(10006, 'Villanueva', 'Andrea Mae', '1988-02-14', '17/85 Stracke Via Suite 042, Poblacion, Las Piñas 4783 Dinagat Islands', '918-621-603', '49-1632020-8', '382189453145', '317-674-022-000', '441093369646', 'Regular', 'HR Manager', 'Lim, Antonio', 52670.00, 1500.00, 1000.00, 1000.00, 26335.00, 313.51),
(10007, 'San Jose', 'Brad', '1996-03-15', '99 Strosin Hills, Poblacion, Bislig 5340 Tawi-Tawi', '797-009-261', '40-2400714-1', '239192926939', '672-474-690-000', '210850209964', 'Regular', 'HR Team Leader', 'Villanueva, Andrea Mae', 42975.00, 1500.00, 800.00, 800.00, 21487.50, 255.80),
(10008, 'Romualdez', 'Alice', '1992-05-14', '12A/33 Upton Isle Apt. 420, Roxas City 1814 Surigao del Norte', '983-606-799', '55-4476527-2', '545652640232', '888-572-294-000', '211385556888', 'Regular', 'HR Rank and File', 'San Jose, Brad', 22500.00, 1500.00, 500.00, 500.00, 11250.00, 133.93),
(10009, 'Atienza', 'Rosie', '1948-09-24', '90A Dibbert Terrace Apt. 190, San Lorenzo 6056 Davao del Norte', '266-036-427', '41-0644692-3', '708988234853', '604-997-793-000', '260107732354', 'Regular', 'HR Rank and File', 'San Jose, Brad', 22500.00, 1500.00, 500.00, 500.00, 11250.00, 133.93),
(10010, 'Alvaro', 'Roderick', '1988-03-30', '#284 T. Morato corner, Scout Rallos Street, Quezon City', '053-381-386', '64-7605054-4', '578114853194', '525-420-419-000', '799254095212', 'Regular', 'Accounting Head', 'Aquino, Bianca Sofia', 52670.00, 1500.00, 1000.00, 1000.00, 26335.00, 313.51),
(10011, 'Salcedo', 'Anthony', '1993-09-14', '93/54 Shanahan Alley Apt. 183, Santo Tomas 1572 Masbate', '070-766-300', '26-9647608-3', '126445315651', '210-805-911-000', '218002473454', 'Regular', 'Payroll Manager', 'Alvaro, Roderick', 50825.00, 1500.00, 1000.00, 1000.00, 25412.50, 302.53),
(10012, 'Lopez', 'Josie', '1987-01-14', '49 Springs Apt. 266, Poblacion, Taguig 3200 Occidental Mindoro', '478-355-427', '44-8563448-3', '431709011012', '218-489-737-000', '113071293354', 'Regular', 'Payroll Team Leader', 'Salcedo, Anthony', 38475.00, 1500.00, 800.00, 800.00, 19237.50, 229.02),
(10013, 'Farala', 'Martha', '1942-01-11', '42/25 Sawayn Stream, Ubay 1208 Zamboanga del Norte', '329-034-366', '45-5656375-0', '233693897247', '210-835-851-000', '631130283546', 'Regular', 'Payroll Rank and File', 'Salcedo, Anthony', 24000.00, 1500.00, 500.00, 500.00, 12000.00, 142.86),
(10014, 'Martinez', 'Leila', '1970-07-11', '37/46 Kulas Roads, Maragondon 0962 Quirino', '877-110-749', '27-2090996-4', '515741057496', '275-792-513-000', '101205445886', 'Regular', 'Payroll Rank and File', 'Salcedo, Anthony', 24000.00, 1500.00, 500.00, 500.00, 12000.00, 142.86),
(10015, 'Romualdez', 'Fredrick', '1985-03-10', '22A/52 Lubowitz Meadows, Pililla 4895 Zambales', '023-079-009', '26-8768374-1', '308366860059', '598-065-761-000', '223057707853', 'Regular', 'Account Manager', 'Lim, Antonio', 53500.00, 1500.00, 1000.00, 1000.00, 26750.00, 318.45),
(10016, 'Mata', 'Christian', '1987-10-21', '90 O\'Keefe Spur Apt. 379, Catigbian 2772 Sulu', '783-776-744', '49-2959312-6', '824187961962', '103-100-522-000', '631052853464', 'Regular', 'Account Team Leader', 'Romualdez, Fredrick', 42975.00, 1500.00, 800.00, 800.00, 21487.50, 255.80),
(10017, 'De Leon', 'Selena', '1975-02-20', '89A Armstrong Trace, Compostela 7874 Maguindanao', '975-432-139', '27-2090208-8', '587272469938', '482-259-498-000', '719007608464', 'Regular', 'Account Team Leader', 'Romualdez, Fredrick', 41850.00, 1500.00, 800.00, 800.00, 20925.00, 249.11),
(10018, 'San Jose', 'Allison', '1986-06-24', '08 Grant Drive Suite 406, Poblacion, Iloilo City 9186 La Union', '179-075-129', '45-3251383-0', '745148459521', '121-203-336-000', '114901859343', 'Regular', 'Account Rank and File', 'Mata, Christian', 22500.00, 1500.00, 500.00, 500.00, 11250.00, 133.93),
(10019, 'Rosario', 'Cydney', '1996-10-06', '93A/21 Berge Points, Tapaz 2180 Quezon', '868-819-912', '49-1629900-2', '579253435499', '122-244-511-000', '265104358643', 'Regular', 'Account Rank and File', 'Mata, Christian', 22500.00, 1500.00, 500.00, 500.00, 11250.00, 133.93),
(10020, 'Bautista', 'Mark', '1991-02-12', '65 Murphy Center Suite 094, Poblacion, Palayan 5636 Quirino', '683-725-348', '49-1647342-5', '399665157135', '273-970-941-000', '260054585575', 'Regular', 'Account Rank and File', 'Mata, Christian', 23250.00, 1500.00, 500.00, 500.00, 11625.00, 138.39),
(10021, 'Lazaro', 'Darlene', '1985-11-25', '47A/94 Larkin Plaza Apt. 179, Poblacion, Caloocan 2751 Quirino', '740-721-558', '45-5617168-2', '606386917510', '354-650-951-000', '104907708845', 'Probationary', 'Account Rank and File', 'Mata, Christian', 23250.00, 1500.00, 500.00, 500.00, 11625.00, 138.39),
(10022, 'Delos Santos', 'Kolby', '1980-02-26', '06A Gulgowski Extensions, Bongabon 6085 Zamboanga del Sur', '739-443-033', '52-0109570-6', '357451271274', '187-500-345-000', '113017988667', 'Probationary', 'Account Rank and File', 'Mata, Christian', 24000.00, 1500.00, 500.00, 500.00, 12000.00, 142.86),
(10023, 'Santos', 'Vella', '1983-12-31', '99A Padberg Spring, Poblacion, Mabalacat 3959 Lanao del Sur', '955-879-269', '52-9883524-3', '548670482885', '101-558-994-000', '360028104576', 'Probationary', 'Account Rank and File', 'Mata, Christian', 22500.00, 1500.00, 500.00, 500.00, 11250.00, 133.93),
(10024, 'Del Rosario', 'Tomas', '1978-12-18', '80A/48 Ledner Ridges, Poblacion, Kabankalan 8870 Marinduque', '882-550-989', '45-5866331-6', '953901539995', '560-735-732-000', '913108649964', 'Probationary', 'Account Rank and File', 'Mata, Christian', 22500.00, 1500.00, 500.00, 500.00, 11250.00, 133.93),
(10025, 'Tolentino', 'Jacklyn', '1984-05-19', '96/48 Watsica Flats Suite 734, Poblacion, Malolos 1844 Ifugao', '675-757-366', '47-1692793-0', '753800654114', '841-177-857-000', '210546661243', 'Probationary', 'Account Rank and File', 'De Leon, Selena', 24000.00, 1500.00, 500.00, 500.00, 12000.00, 142.86),
(10026, 'Gutierrez', 'Percival', '1970-12-18', '58A Wilderman Walks, Poblacion, Digos 5822 Davao del Sur', '512-899-876', '40-9504657-8', '797639382265', '502-995-671-000', '210897095686', 'Probationary', 'Account Rank and File', 'De Leon, Selena', 24750.00, 1500.00, 500.00, 500.00, 12375.00, 147.32),
(10027, 'Manalaysay', 'Garfield', '1986-08-28', '60 Goyette Valley Suite 219, Poblacion, Tabuk 3159 Lanao del Sur', '948-628-136', '45-3298166-4', '810909286264', '336-676-445-000', '211274476563', 'Probationary', 'Account Rank and File', 'De Leon, Selena', 24750.00, 1500.00, 500.00, 500.00, 12375.00, 147.32),
(10028, 'Villegas', 'Lizeth', '1981-12-12', '66/77 Mann Views, Luisiana 1263 Dinagat Islands', '332-372-215', '40-2400719-4', '934389652994', '210-395-397-000', '122238077997', 'Probationary', 'Account Rank and File', 'De Leon, Selena', 24000.00, 1500.00, 500.00, 500.00, 12000.00, 142.86),
(10029, 'Ramos', 'Carol', '1978-08-20', '72/70 Stamm Spurs, Bustos 4550 Iloilo', '250-700-389', '60-1152206-4', '351830469744', '395-032-717-000', '212141893454', 'Probationary', 'Account Rank and File', 'De Leon, Selena', 22500.00, 1500.00, 500.00, 500.00, 11250.00, 133.93),
(10030, 'Maceda', 'Emelia', '1973-04-14', '50A/83 Bahringer Oval Suite 145, Kiamba 7688 Nueva Ecija', '973-358-041', '54-1331005-0', '465087894112', '215-973-013-000', '515012579765', 'Probationary', 'Account Rank and File', 'De Leon, Selena', 22500.00, 1500.00, 500.00, 500.00, 11250.00, 133.93),
(10031, 'Aguilar', 'Delia', '1989-01-27', '95 Cremin Junction, Surallah 2809 Cotabato', '529-705-439', '52-1859253-1', '136451303068', '599-312-588-000', '110018813465', 'Probationary', 'Account Rank and File', 'De Leon, Selena', 22500.00, 1500.00, 500.00, 500.00, 11250.00, 133.93),
(10032, 'Castro', 'John Rafael', '1992-02-09', 'Hi-way, Yati, Liloan Cebu', '332-424-955', '26-7145133-4', '601644902402', '404-768-309-000', '697764069311', 'Regular', 'Sales & Marketing', 'Reyes, Isabella', 52670.00, 1500.00, 1000.00, 1000.00, 26335.00, 313.51),
(10033, 'Martinez', 'Carlos Ian', '1990-11-16', 'Bulala, Camalaniugan', '078-854-208', '11-5062972-7', '380685387212', '256-436-296-000', '993372963726', 'Regular', 'Supply Chain and Logistics', 'Reyes, Isabella', 52670.00, 1500.00, 1000.00, 1000.00, 26335.00, 313.51),
(10034, 'Santos', 'Beatriz', '1990-08-07', 'Agapita Building, Metro Manila', '526-639-511', '20-2987501-5', '918460050077', '911-529-713-000', '874042259378', 'Regular', 'Customer Service and Relations', 'Reyes, Isabella', 52670.00, 1500.00, 1000.00, 1000.00, 26335.00, 313.51);

-- Verify employees were inserted
SELECT COUNT(*) as employee_count FROM employees;

-- =============================================
-- Create credentials table
-- =============================================
CREATE TABLE credentials (
    employee_id INT PRIMARY KEY,
    password VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (employee_id) REFERENCES employees(employee_id) ON DELETE CASCADE
);

-- =============================================
-- Insert credentials for all employees
-- =============================================
INSERT INTO credentials (employee_id, password) VALUES
(10001, 'password1234'),
(10002, 'password1234'),
(10003, 'password1234'),
(10004, 'password1234'),
(10005, 'password1234'),
(10006, 'password1234'),
(10007, 'password1234'),
(10008, 'password1234'),
(10009, 'password1234'),
(10010, 'password1234'),
(10011, 'password1234'),
(10012, 'password1234'),
(10013, 'password1234'),
(10014, 'password1234'),
(10015, 'password1234'),
(10016, 'password1234'),
(10017, 'password1234'),
(10018, 'password1234'),
(10019, 'password1234'),
(10020, 'password1234'),
(10021, 'password1234'),
(10022, 'password1234'),
(10023, 'password1234'),
(10024, 'password1234'),
(10025, 'password1234'),
(10026, 'password1234'),
(10027, 'password1234'),
(10028, 'password1234'),
(10029, 'password1234'),
(10030, 'password1234'),
(10031, 'password1234'),
(10032, 'password1234'),
(10033, 'password1234'),
(10034, 'password1234');

-- Verify credentials were inserted
SELECT COUNT(*) as credential_count FROM credentials;

-- =============================================
-- Create remaining tables
-- =============================================

-- Leave request table
CREATE TABLE leave_request (
    leave_id INT AUTO_INCREMENT PRIMARY KEY,
    employee_id INT NOT NULL,
    leave_type VARCHAR(50),
    start_date DATE,
    end_date DATE,
    status ENUM('Pending', 'Approved', 'Rejected') DEFAULT 'Pending',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (employee_id) REFERENCES employees(employee_id) ON DELETE CASCADE
);

-- Attendance table
CREATE TABLE attendance (
    id INT AUTO_INCREMENT PRIMARY KEY,
    employee_id INT NOT NULL,
    date DATE NOT NULL,
    log_in TIME NOT NULL,
    log_out TIME NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (employee_id) REFERENCES employees(employee_id) ON DELETE CASCADE
);

-- Insert sample attendance data
INSERT INTO attendance (employee_id, date, log_in, log_out) VALUES
(10001, '2024-06-03', '08:59:00', '18:31:00'),
(10002, '2024-06-03', '10:35:00', '19:44:00'),
(10003, '2024-06-03', '10:23:00', '18:32:00'),
(10004, '2024-06-03', '10:57:00', '18:14:00'),
(10005, '2024-06-03', '09:48:00', '17:13:00'),
(10006, '2024-06-03', '09:31:00', '19:29:00'),
(10007, '2024-06-03', '09:09:00', '16:30:00'),
(10008, '2024-06-03', '09:02:00', '18:06:00'),
(10009, '2024-06-03', '08:18:00', '17:40:00'),
(10010, '2024-06-03', '08:10:00', '15:13:00'),
(10011, '2024-06-03', '09:08:00', '19:36:00'),
(10012, '2024-06-03', '09:47:00', '18:43:00'),
(10013, '2024-06-03', '09:48:00', '19:21:00'),
(10014, '2024-06-03', '09:23:00', '18:09:00'),
(10015, '2024-06-03', '08:41:00', '19:27:00'),
(10016, '2024-06-03', '08:41:00', '16:45:00'),
(10017, '2024-06-03', '09:40:00', '17:24:00'),
(10018, '2024-06-03', '08:22:00', '16:46:00'),
(10019, '2024-06-03', '09:53:00', '17:24:00'),
(10020, '2024-06-03', '08:47:00', '16:27:00'),
(10021, '2024-06-03', '09:37:00', '18:45:00'),
(10022, '2024-06-03', '10:54:00', '20:10:00'),
(10023, '2024-06-03', '10:27:00', '20:10:00'),
(10024, '2024-06-03', '09:16:00', '17:57:00'),
(10025, '2024-06-03', '10:18:00', '18:07:00'),
(10026, '2024-06-03', '08:17:00', '18:31:00'),
(10027, '2024-06-03', '09:05:00', '19:14:00'),
(10028, '2024-06-03', '08:52:00', '17:23:00'),
(10029, '2024-06-03', '10:57:00', '21:44:00'),
(10030, '2024-06-03', '08:29:00', '16:46:00'),
(10031, '2024-06-03', '10:07:00', '20:51:00'),
(10032, '2024-06-03', '08:29:00', '16:46:00'),
(10033, '2024-06-03', '10:02:00', '19:39:00'),
(10034, '2024-06-03', '10:05:00', '18:12:00');

-- Additional tables
CREATE TABLE overtime (
    overtime_id INT AUTO_INCREMENT PRIMARY KEY,
    employee_id INT NOT NULL,
    date DATE NOT NULL,
    hours DECIMAL(5,2) NOT NULL,
    reason TEXT,
    approved BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (employee_id) REFERENCES employees(employee_id) ON DELETE CASCADE
);

CREATE TABLE deductions (
    deduction_id INT AUTO_INCREMENT PRIMARY KEY,
    employee_id INT NOT NULL,
    type VARCHAR(50) NOT NULL,
    amount DECIMAL(8,2) NOT NULL,
    description TEXT,
    deduction_date DATE DEFAULT (CURRENT_DATE),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (employee_id) REFERENCES employees(employee_id) ON DELETE CASCADE
);

CREATE TABLE government_contributions (
    contribution_id INT AUTO_INCREMENT PRIMARY KEY,
    employee_id INT NOT NULL,
    sss DECIMAL(8,2) DEFAULT 0,
    philhealth DECIMAL(8,2) DEFAULT 0,
    pagibig DECIMAL(8,2) DEFAULT 0,
    tax DECIMAL(8,2) DEFAULT 0,
    contribution_period DATE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (employee_id) REFERENCES employees(employee_id) ON DELETE CASCADE
);

CREATE TABLE compensation_details (
    compensation_details_id INT AUTO_INCREMENT PRIMARY KEY,
    employee_id INT NOT NULL,
    rice_subsidy DECIMAL(8,2) DEFAULT 0,
    phone_allowance DECIMAL(8,2) DEFAULT 0,
    clothing_allowance DECIMAL(8,2) DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (employee_id) REFERENCES employees(employee_id) ON DELETE CASCADE
);

CREATE TABLE payroll (
    payroll_id INT AUTO_INCREMENT PRIMARY KEY,
    employee_id INT NOT NULL,
    period_start DATE NOT NULL,
    period_end DATE NOT NULL,
    monthly_rate DECIMAL(10,2) DEFAULT 0,
    days_worked INT DEFAULT 0,
    overtime_hours DECIMAL(5,2) DEFAULT 0,
    gross_pay DECIMAL(10,2) DEFAULT 0,
    total_deductions DECIMAL(10,2) DEFAULT 0,
    net_pay DECIMAL(10,2) DEFAULT 0,
    gross_earnings DECIMAL(10,2) DEFAULT 0,
    late_deduction DECIMAL(8,2) DEFAULT 0,
    undertime_deduction DECIMAL(8,2) DEFAULT 0,
    unpaid_leave_deduction DECIMAL(8,2) DEFAULT 0,
    overtime_pay DECIMAL(8,2) DEFAULT 0,
    rice_subsidy DECIMAL(8,2) DEFAULT 0,
    phone_allowance DECIMAL(8,2) DEFAULT 0,
    clothing_allowance DECIMAL(8,2) DEFAULT 0,
    sss DECIMAL(8,2) DEFAULT 0,
    philhealth DECIMAL(8,2) DEFAULT 0,
    pagibig DECIMAL(8,2) DEFAULT 0,
    tax DECIMAL(8,2) DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (employee_id) REFERENCES employees(employee_id) ON DELETE CASCADE,
    UNIQUE KEY unique_payroll_period (employee_id, period_start, period_end)
);

-- Insert compensation details from employee data
INSERT INTO compensation_details (employee_id, rice_subsidy, phone_allowance, clothing_allowance)
SELECT employee_id, rice_subsidy, phone_allowance, clothing_allowance FROM employees;

-- Insert sample overtime data
INSERT INTO overtime (employee_id, date, hours, reason, approved) VALUES
(10001, '2024-06-03', 2.5, 'Project deadline preparation', TRUE),
(10002, '2024-06-03', 1.5, 'Monthly report completion', TRUE),
(10005, '2024-06-04', 3.0, 'System maintenance', TRUE);

-- =============================================
-- Create indexes for better performance
-- =============================================
CREATE INDEX idx_employees_last_name ON employees(last_name);
CREATE INDEX idx_employees_position ON employees(position);
CREATE INDEX idx_employees_status ON employees(status);
CREATE INDEX idx_employees_supervisor ON employees(immediate_supervisor);
CREATE INDEX idx_leave_request_employee_id ON leave_request(employee_id);
CREATE INDEX idx_leave_request_dates ON leave_request(start_date, end_date);
CREATE INDEX idx_attendance_employee_date ON attendance(employee_id, date);
CREATE INDEX idx_attendance_date ON attendance(date);
CREATE INDEX idx_payroll_employee_id ON payroll(employee_id);
CREATE INDEX idx_payroll_period ON payroll(period_start, period_end);
CREATE INDEX idx_overtime_employee_id ON overtime(employee_id);
CREATE INDEX idx_deductions_employee_id ON deductions(employee_id);
CREATE INDEX idx_government_contributions_employee ON government_contributions(employee_id);
CREATE INDEX idx_compensation_details_employee ON compensation_details(employee_id);

-- Re-enable foreign key checks
SET FOREIGN_KEY_CHECKS = 1;
SET sql_mode = 'STRICT_TRANS_TABLES,NO_ZERO_DATE,NO_ZERO_IN_DATE,ERROR_FOR_DIVISION_BY_ZERO';

-- =============================================
-- Verification Queries
-- =============================================
SELECT 'Database Setup Complete!' as Status;

-- Show table row counts
SELECT 'employees' as table_name, COUNT(*) as row_count FROM employees
UNION ALL
SELECT 'credentials' as table_name, COUNT(*) as row_count FROM credentials
UNION ALL
SELECT 'leave_request' as table_name, COUNT(*) as row_count FROM leave_request
UNION ALL
SELECT 'attendance' as table_name, COUNT(*) as row_count FROM attendance
UNION ALL
SELECT 'payroll' as table_name, COUNT(*) as row_count FROM payroll
UNION ALL
SELECT 'overtime' as table_name, COUNT(*) as row_count FROM overtime
UNION ALL
SELECT 'deductions' as table_name, COUNT(*) as row_count FROM deductions
UNION ALL
SELECT 'government_contributions' as table_name, COUNT(*) as row_count FROM government_contributions
UNION ALL
SELECT 'compensation_details' as table_name, COUNT(*) as row_count FROM compensation_details;

-- Test authentication query
SELECT 'Authentication Test' as test_type, COUNT(*) as available_logins 
FROM credentials c 
JOIN employees e ON c.employee_id = e.employee_id;

-- Sample login verification for Employee 10001
SELECT e.employee_id, e.first_name, e.last_name, e.position, c.password
FROM employees e 
JOIN credentials c ON e.employee_id = c.employee_id 
WHERE e.employee_id = 10001;