package com.example.ems.attendance;

import com.example.ems.employee.Employee;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;

public interface AttendanceRepository extends JpaRepository<Attendance, Long> {
    Optional<Attendance> findByEmployeeAndDate(Employee employee, LocalDate date);
    Page<Attendance> findByEmployeeAndDateBetween(Employee employee, LocalDate start, LocalDate end, Pageable pageable);
    Page<Attendance> findByEmployeeIdAndDateBetween(Long employeeId, LocalDate start, LocalDate end, Pageable pageable);
//    Page<Attendance> findByDateBetween(LocalDate start, LocalDate end, Pageable pageable);
}
