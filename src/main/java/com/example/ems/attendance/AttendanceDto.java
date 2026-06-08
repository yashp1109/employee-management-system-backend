package com.example.ems.attendance;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record AttendanceDto(
        Long id,
        Long employeeId,
        String employeeName,
        LocalDateTime date,
        String dateOnly,
        String checkInTime,
        String checkOutTime,
        BigDecimal workingHours,
        String message
) {
}
