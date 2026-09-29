package com.example.ems.attendance;

import com.example.ems.common.ApiResonse;
import com.example.ems.common.BadRequestException;
import com.example.ems.common.Role;
import com.example.ems.employee.Employee;
import com.example.ems.employee.EmployeeRepository;
import com.example.ems.security.CurrentUserService;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
public class AttendanceService {
    private final AttendanceRepository repository;
    private final EmployeeRepository employeeRepository;
    private final CurrentUserService currentUserService;

    @Transactional
    public ApiResonse<Object> checkIn() {

        Employee employee = currentUserService.employee();
        LocalDate today = LocalDate.now();
        if (repository.findByEmployeeAndDate(employee, today).isPresent()) {
           return ApiResonse.builder().message("Already Checked In Today")
                    .success(HttpStatus.OK.value()).build();
        }
        Attendance attendance = new Attendance();
        attendance.setEmployee(employee);
        attendance.setDate(today);
        attendance.setCheckInTime(LocalTime.now());
        repository.save(attendance);
        return ApiResonse.builder().message("Check-In Successfully")
                .success(HttpStatus.OK.value()).build();
    }

    @Transactional
    public ApiResonse<Object> checkOut() {
        Employee employee = currentUserService.employee();

        Attendance attendance = repository.findByEmployeeAndDate(employee, LocalDate.now()).orElse(null);
        if (attendance == null) {
            return ApiResonse.builder().message("Please CheckIn First")
                    .success(HttpStatus.OK.value()).build();
        }else if(attendance !=null && attendance.getCheckOutTime() != null){
            return ApiResonse.builder().message("Already Check Out")
                    .success(HttpStatus.OK.value()).build();
        }
        attendance.setCheckOutTime(LocalTime.now());
        long minutes = Duration.between(attendance.getCheckInTime(), attendance.getCheckOutTime()).toMinutes();
        attendance.setWorkingHours(BigDecimal.valueOf(minutes).divide(BigDecimal.valueOf(60), 2, RoundingMode.HALF_UP));
        repository.save(attendance);
        return ApiResonse.builder().message("Done Check Out")
                .success(HttpStatus.OK.value()).build();
    }

    @Transactional(readOnly = true)
    public Page<AttendanceDto> history(Long employeeId, LocalDate start, LocalDate end, Pageable pageable) {
        Employee currentUser = currentUserService.employee();
        boolean isAdminOrHr = currentUser.getRole() == Role.ADMIN || currentUser.getRole() == Role.HR;
        if (employeeId != null) {
            if (!isAdminOrHr && !employeeId.equals(currentUser.getId())) {
                Employee target = employeeRepository.findById(employeeId)
                        .orElseThrow(() -> new BadRequestException("Employee not found"));
                if (currentUser.getRole() != Role.MANAGER
                        || currentUser.getDepartment() == null
                        || target.getDepartment() == null
                        || !currentUser.getDepartment().getId().equals(target.getDepartment().getId())) {
                    throw new AccessDeniedException("Not authorized to view attendance for this employee");
                }
            }
            return repository.findByEmployeeIdAndDateBetween(employeeId, start, end, pageable).map(this::toDto);
        }

        return repository.findByEmployeeAndDateBetween(currentUser, start, end, pageable).map(this::toDto);
    }

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    private AttendanceDto toDto(Attendance a) {
        LocalDateTime dateTime = a.getDate().atTime(a.getCheckInTime() != null ? a.getCheckInTime() : LocalTime.MIDNIGHT);
        return new AttendanceDto(
                a.getId(),
                a.getEmployee().getId(),
                a.getEmployee().getFirstName() + " " + a.getEmployee().getLastName(),
                dateTime,
                a.getDate().toString(),
                a.getCheckInTime() == null ? null : a.getCheckInTime().format(TIME_FORMATTER),
                a.getCheckOutTime() == null ? null : a.getCheckOutTime().format(TIME_FORMATTER),
                a.getWorkingHours(),
                a.getMessage()
        );
    }
}
