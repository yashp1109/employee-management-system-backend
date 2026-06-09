package com.example.ems.employee;

import com.example.ems.common.Gender;
import com.example.ems.common.Role;
import com.example.ems.common.Status;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;

public record EmployeeRequest(
        @NotBlank @Size(max = 30) String employeeCode,
        @NotBlank @Size(max = 80) String firstName,
        @NotBlank @Size(max = 80) String lastName,
        @Email @NotBlank @Size(max = 160) String email,
        @Pattern(regexp = "^$|^[0-9+\\- ]{8,20}$", message = "Mobile number is invalid") String mobileNumber,
        @NotNull Gender gender,
        @Past LocalDate dateOfBirth,
        @NotNull LocalDate dateOfJoining,
        @NotNull Long departmentId,
        Long designationId,
        @NotNull @DecimalMin("0.0") BigDecimal salary,
        Long managerId,
        @NotNull Status status,
        @NotNull Role role,
        @Size(min = 8, max = 72) String password
) {
}
