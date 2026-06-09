package com.example.ems.config;

import com.example.ems.common.Gender;
import com.example.ems.common.Role;
import com.example.ems.common.Status;
import com.example.ems.department.DepartmentRepository;
import com.example.ems.designation.DesignationRepository;
import com.example.ems.employee.Employee;
import com.example.ems.employee.EmployeeRepository;
import com.example.ems.leave.LeaveBalance;
import com.example.ems.leave.LeaveBalanceRepository;
import com.example.ems.leave.LeaveTypeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.time.LocalDate;

@Configuration
@RequiredArgsConstructor
public class DataInitializer {
    private final EmployeeRepository employeeRepository;
    private final DepartmentRepository departmentRepository;
    private final DesignationRepository designationRepository;
    private final LeaveTypeRepository leaveTypeRepository;
    private final LeaveBalanceRepository leaveBalanceRepository;
    private final PasswordEncoder passwordEncoder;

    @Bean
    CommandLineRunner seedUsers() {
        return args -> {
            if (employeeRepository.count() > 0) {
                return;
            }

            Employee admin = employeeRepository.save(employee("EMP001", "Admin", "User", "admin@ems.local", Role.ADMIN, 1L, 1L, null, LocalDate.of(2017, 3, 1)));
            Employee hr = employeeRepository.save(employee("EMP002", "Harini", "Rao", "hr@ems.local", Role.HR, 2L, 2L, admin, LocalDate.of(2018, 6, 12)));
            Employee manager = employeeRepository.save(employee("EMP003", "Manav", "Sharma", "manager@ems.local", Role.MANAGER, 3L, 3L, admin, LocalDate.of(2019, 1, 20)));
            employeeRepository.save(employee("EMP004", "Elina", "Das", "employee@ems.local", Role.EMPLOYEE, 3L, 4L, manager, LocalDate.of(2021, 8, 22)));

            // Finance lead (Finance department has no designation seeded)
            Employee financeLead = employeeRepository.save(employee("EMP005", "Robert", "Hall", "robert.hall@ems.local", Role.MANAGER, 4L, null, admin, LocalDate.of(2018, 11, 5)));
            // Additional HR manager
            Employee hrLead = employeeRepository.save(employee("EMP006", "Sarah", "Miller", "sarah.miller@ems.local", Role.MANAGER, 2L, 2L, admin, LocalDate.of(2019, 5, 10)));

            // Engineering team under Manav
            employeeRepository.save(employee("EMP007", "John", "Smith", "john.smith@ems.local", Role.EMPLOYEE, 3L, 4L, manager, LocalDate.of(2020, 2, 15)));
            employeeRepository.save(employee("EMP008", "Emma", "Johnson", "emma.johnson@ems.local", Role.EMPLOYEE, 3L, 4L, manager, LocalDate.of(2021, 11, 5)));
            employeeRepository.save(employee("EMP009", "Michael", "Brown", "michael.brown@ems.local", Role.EMPLOYEE, 3L, 4L, manager, LocalDate.of(2022, 3, 18)));
            employeeRepository.save(employee("EMP010", "Sophia", "Davis", "sophia.davis@ems.local", Role.EMPLOYEE, 3L, 4L, manager, LocalDate.of(2018, 7, 30)));
            employeeRepository.save(employee("EMP011", "James", "Taylor", "james.taylor@ems.local", Role.EMPLOYEE, 3L, 4L, manager, LocalDate.of(2020, 9, 30)));

            // Finance team under Robert Hall
            employeeRepository.save(employee("EMP012", "Olivia", "Martinez", "olivia.martinez@ems.local", Role.EMPLOYEE, 4L, null, financeLead, LocalDate.of(2017, 6, 24)));
            employeeRepository.save(employee("EMP013", "Daniel", "Wilson", "daniel.wilson@ems.local", Role.EMPLOYEE, 4L, null, financeLead, LocalDate.of(2022, 1, 18)));
            employeeRepository.save(employee("EMP014", "Ava", "Thompson", "ava.thompson@ems.local", Role.EMPLOYEE, 4L, null, financeLead, LocalDate.of(2019, 12, 12)));

            // HR team under Sarah Miller
            employeeRepository.save(employee("EMP015", "Liam", "Anderson", "liam.anderson@ems.local", Role.EMPLOYEE, 2L, 2L, hrLead, LocalDate.of(2021, 4, 1)));
            employeeRepository.save(employee("EMP016", "Mia", "Thomas", "mia.thomas@ems.local", Role.EMPLOYEE, 2L, 2L, hrLead, LocalDate.of(2020, 10, 19)));

            // Administration staff under admin
            employeeRepository.save(employee("EMP017", "Noah", "Jackson", "noah.jackson@ems.local", Role.EMPLOYEE, 1L, 1L, admin, LocalDate.of(2018, 2, 14)));
            employeeRepository.save(employee("EMP018", "Isabella", "White", "isabella.white@ems.local", Role.EMPLOYEE, 1L, 1L, admin, LocalDate.of(2023, 1, 9)));

            employeeRepository.findAll().forEach(this::createBalances);
        };
    }

    private Employee employee(String code, String first, String last, String email, Role role, Long departmentId, Long designationId, Employee manager, LocalDate dateOfJoining) {
        Employee employee = new Employee();
        employee.setEmployeeCode(code);
        employee.setFirstName(first);
        employee.setLastName(last);
        employee.setEmail(email);
        employee.setMobileNumber("9999999999");
        employee.setGender(Gender.OTHER);
        employee.setDateOfBirth(LocalDate.of(1995, 1, 1));
        employee.setDateOfJoining(dateOfJoining);
        employee.setDepartment(departmentRepository.findById(departmentId).orElseThrow());
        employee.setDesignation(designationId == null ? null : designationRepository.findById(designationId).orElseThrow());
        employee.setSalary(BigDecimal.valueOf(75000));
        employee.setManager(manager);
        employee.setStatus(Status.ACTIVE);
        employee.setRole(role);
        employee.setPasswordHash(passwordEncoder.encode("Password@123"));
        return employee;
    }

    private void createBalances(Employee employee) {
        int year = LocalDate.now().getYear();
        leaveTypeRepository.findAll().forEach(type -> {
            LeaveBalance balance = new LeaveBalance();
            balance.setEmployee(employee);
            balance.setLeaveType(type);
            balance.setYear(year);
            balance.setTotalDays(type.getAnnualQuota());
            balance.setUsedDays(0);
            leaveBalanceRepository.save(balance);
        });
    }
}
