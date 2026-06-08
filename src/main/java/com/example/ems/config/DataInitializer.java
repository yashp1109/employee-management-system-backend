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

            Employee admin = employeeRepository.save(employee("EMP001", "Admin", "User", "admin@ems.local", Role.ADMIN, 1L, 1L, null));
            Employee hr = employeeRepository.save(employee("EMP002", "Harini", "Rao", "hr@ems.local", Role.HR, 2L, 2L, admin));
            Employee manager = employeeRepository.save(employee("EMP003", "Manav", "Sharma", "manager@ems.local", Role.MANAGER, 3L, 3L, admin));
            employeeRepository.save(employee("EMP004", "Elina", "Das", "employee@ems.local", Role.EMPLOYEE, 3L, 4L, manager));

            employeeRepository.findAll().forEach(this::createBalances);
        };
    }

    private Employee employee(String code, String first, String last, String email, Role role, Long departmentId, Long designationId, Employee manager) {
        Employee employee = new Employee();
        employee.setEmployeeCode(code);
        employee.setFirstName(first);
        employee.setLastName(last);
        employee.setEmail(email);
        employee.setMobileNumber("9999999999");
        employee.setGender(Gender.OTHER);
        employee.setDateOfBirth(LocalDate.of(1995, 1, 1));
        employee.setDateOfJoining(LocalDate.now().minusYears(1));
        employee.setDepartment(departmentRepository.findById(departmentId).orElseThrow());
        employee.setDesignation(designationRepository.findById(designationId).orElseThrow());
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
