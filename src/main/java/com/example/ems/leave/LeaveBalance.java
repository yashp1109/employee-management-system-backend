package com.example.ems.leave;

import com.example.ems.employee.Employee;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "leave_balances", uniqueConstraints = {
        @UniqueConstraint(name = "uk_leave_balance_employee_type_year", columnNames = {"employee_id", "leave_type_id", "year"})
})
public class LeaveBalance {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id")
    private Employee employee;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "leave_type_id")
    private LeaveType leaveType;

    @Column(nullable = false)
    private Integer year;

    @Column(nullable = false)
    private Integer totalDays;

    @Column(nullable = false)
    private Integer usedDays = 0;

    public Integer getRemainingDays() {
        return totalDays - usedDays;
    }
}
