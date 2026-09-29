package com.example.ems.employee;

import com.example.ems.common.Gender;
import com.example.ems.common.Role;
import com.example.ems.common.Status;
import com.example.ems.department.Department;
import com.example.ems.designation.Designation;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@Getter
@Setter
@Entity
@Table(name = "employees", indexes = {
        @Index(name = "idx_employee_email", columnList = "email"),
        @Index(name = "idx_employee_code", columnList = "employee_code")
})
public class Employee {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "employee_code", nullable = false, unique = true, length = 30)
    private String employeeCode;

    @Column(nullable = false, length = 80)
    private String firstName;

    @Column(nullable = false, length = 80)
    private String lastName;

    @Column(nullable = false, unique = true, length = 160)
    private String email;

    @Column(length = 20)
    private String mobileNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Gender gender;

    private LocalDate dateOfBirth;
    private LocalDate dateOfJoining;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "department_id")
    private Department department;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "designation_id")
    private Designation designation;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal salary;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "manager_id")
    private Employee manager;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Status status = Status.ACTIVE;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Role role = Role.EMPLOYEE;

    @Column(nullable = false)
    private String passwordHash;

    @CreationTimestamp
    private Instant createdAt;

    @UpdateTimestamp
    private Instant updatedAt;
}
