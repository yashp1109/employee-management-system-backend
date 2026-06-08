package com.example.ems.employee;

import com.example.ems.common.Gender;
import com.example.ems.common.Role;
import com.example.ems.common.Status;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDate;
@Builder
public record EmployeeDto(
        Long id,
        String employeeCode,
        String firstName,
        String lastName,
        String email,
        String mobileNumber,
        Gender gender,
        LocalDate dateOfBirth,
        LocalDate dateOfJoining,
        Long departmentId,
        String departmentName,
        Long designationId,
        String designationName,
        BigDecimal salary,
        Long managerId,
        String managerName,
        Status status,
        Role role
) {
}
