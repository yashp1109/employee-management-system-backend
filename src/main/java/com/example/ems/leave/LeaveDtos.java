package com.example.ems.leave;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public class LeaveDtos {
    public record LeaveRequestDto(
            Long id,
            Long employeeId,
            String employeeName,
            Long leaveTypeId,
            String leaveTypeName,
            LocalDate startDate,
            LocalDate endDate,
            Integer days,
            String reason,
            LeaveStatus status
    ) {
    }

    public record ApplyLeaveRequest(
            @NotNull Long leaveTypeId,
            @NotNull LocalDate startDate,
            @NotNull LocalDate endDate,
            @NotBlank String reason
    ) {
    }

    public record LeaveBalanceDto(Long leaveTypeId, String leaveTypeName, Integer totalDays, Integer usedDays, Integer remainingDays) {
    }
}
