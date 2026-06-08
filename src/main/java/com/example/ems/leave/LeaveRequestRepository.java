package com.example.ems.leave;

import com.example.ems.employee.Employee;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LeaveRequestRepository extends JpaRepository<LeaveRequest, Long> {
    Page<LeaveRequest> findByEmployee(Employee employee, Pageable pageable);
    Page<LeaveRequest> findByEmployeeId(Long employeeId, Pageable pageable);
    Page<LeaveRequest> findByStatus(LeaveStatus status, Pageable pageable);
    Page<LeaveRequest> findByEmployeeManagerAndStatus(Employee manager, LeaveStatus status, Pageable pageable);
    long countByStatus(LeaveStatus status);
}
