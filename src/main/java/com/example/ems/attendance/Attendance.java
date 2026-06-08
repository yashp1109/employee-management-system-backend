package com.example.ems.attendance;

import com.example.ems.employee.Employee;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
@Entity
@Table(name = "attendances", uniqueConstraints = {
        @UniqueConstraint(name = "uk_attendance_employee_date", columnNames = {"employee_id", "attendance_date"})
})
public class Attendance {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id")
    private Employee employee;

    @Column(name = "attendance_date", nullable = false)
    private LocalDate date;

    private LocalTime checkInTime;

    private LocalTime checkOutTime;

    @Column(precision = 5, scale = 2)
    private BigDecimal workingHours;

    @Column(columnDefinition = "text")
    private String message;
}
