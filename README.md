# MotorPH Payroll System

## 🚀 Advanced Object-Oriented Programming Project
**Course:** MO-IT113 - Advanced Object-Oriented Programming  
**Institution:** Mapúa Malayan Digital College  
**Academic Year:** 2024-2025

## 👥 Development Team - Group 6 (Section A2101)
- **Baguio, Gilliane Rose**
- **Celocia, Jannine Claire**  
- **Cabugnason, Rick**
- **Decastillo, Pamela Loraine**
- **Manalaysay, Rejoice**

## 📋 Project Overview

The MotorPH Payroll System is a comprehensive Java-based desktop application designed to manage employee payroll, attendance, and HR operations for MotorPH company. The system has been enhanced from a CSV-based storage solution to a robust MySQL database-driven application with advanced features and improved scalability.

### 🎯 Key Features

#### Employee Management
- **Complete Employee Records**: Personal information, employment status, position details
- **Role-Based Access Control**: Separate dashboards for employees and HR personnel
- **Employee Search & Filtering**: Advanced search capabilities with multiple criteria
- **Password Management**: Secure credential handling with change password functionality

#### Payroll Processing
- **Automated Payroll Calculation**: Comprehensive salary computation including:
  - Basic salary calculation based on days worked
  - Overtime pay with 125% rate multiplier
  - Government contributions (SSS, PhilHealth, Pag-IBIG)
  - Tax calculations using TRAIN law brackets
  - Allowances (Rice subsidy, Phone allowance, Clothing allowance)
  - Time-based deductions (Late, Undertime, Unpaid leave)
- **Detailed Payslips**: Professional payslip generation with company branding
- **Print & Export**: Print payslips or save for records

#### Attendance Tracking
- **Digital Time Logging**: Log in/log out time tracking
- **Attendance Analytics**: Work hours calculation, late/undertime detection
- **Attendance Reports**: Comprehensive attendance summaries and statistics
- **Bulk Attendance Management**: HR can manage attendance for all employees

#### Leave Management
- **Leave Request System**: Employee self-service leave application
- **Approval Workflow**: HR approval/rejection with status tracking
- **Leave Types**: Annual, Sick, Emergency, Maternity, Paternity leave
- **Overlap Detection**: Prevents conflicting leave requests

#### Reporting & Analytics
- **Monthly Payroll Reports**: Comprehensive payroll summaries
- **Export Options**: CSV and HTML export formats
- **Detailed Attendance Reports**: Attendance summary of all the employees
- **Government Contributions Reports**: SSS, PhilHealth, and Pag-IBIG reports

## 🛠️ Installation & Setup

### Prerequisites
- **Java Development Kit (JDK) 8 or higher**
- **MySQL Server 8.0+**
- **MySQL Workbench** (recommended)
- **MySQL Connector/J** (JDBC Driver)

### Database Setup

1. **Download the SQL Setup Script**
   ```
   File: aoopdatabase_payroll.sql
   Location: src/util/aoopdatabase_payroll.sql
   ```

2. **Configure MySQL Connection**
   - **Host**: localhost
   - **Port**: 3306
   - **Username**: root
   - **Password**: admin
   - **Database**: aoopdatabase_payroll

3. **Execute Setup Script**
   - Open MySQL Workbench
   - Connect to your MySQL server
   - Open and execute `aoopdatabase_payroll.sql`
   - Verify successful creation of database and tables

4. **Verify Installation**
   ```sql
   USE aoopdatabase_payroll;
   SELECT COUNT(*) FROM employees; -- Should return 34
   SELECT COUNT(*) FROM credentials; -- Should return 34
   SELECT COUNT(*) FROM attendance; -- Should return 34+
   ```

### Application Setup

1. **Clone/Download Project**
   ```bash
   git clone <repository-url>
   cd motorph-payroll-system
   ```

2. **Add MySQL Connector**
   - Download `mysql-connector-java.jar`
   - Add to project classpath
   - Ensure it's included in build path

3. **Configure Database Connection**
   - Update `src/util/DBConnection.java` if needed
   - Default configuration:
     ```java
     HOST = "localhost"
     PORT = "3306"
     DATABASE_NAME = "aoopdatabase_payroll"
     USER = "root"
     PASSWORD = "admin"
     ```

4. **Compile and Run**
   ```bash
   # Compile
   javac -cp ".:mysql-connector-java.jar" src/**/*.java
   
   # Run
   java -cp ".:mysql-connector-java.jar:src" ui.MainApplication
   ```

## 🔐 Default Login Credentials

The system comes with pre-configured test accounts:

### Employee Accounts
- **Employee IDs**: 10001 to 10034
- **Default Password**: `password1234`

### Sample HR/Management Accounts
- **Employee ID**: 10001 (CEO)
- **Employee ID**: 10002 (COO)  
- **Employee ID**: 10003 (CFO)
- **Employee ID**: 10006 (HR Manager)
- **Password**: `password1234`


## 🚀 Future Enhancements

### Planned Features
- **Enhanced Reporting System**: 
  - Employee performance dashboards
  - Custom report builder
- **Web-based Interface**: Browser-accessible application
- **Biometric Integration**: Fingerprint/face recognition for attendance
- **Mobile Application**: Mobile app for employee self-service
- **Advanced Reporting**: Business intelligence dashboards
- **API Integration**: External system integrations
- **Cloud Deployment**: Cloud-based hosting options


## 📄 License

Academic use only. Developed by Group 6, Mapúa Malayan Digital College.

**© 2025 MotorPH Payroll System**  
