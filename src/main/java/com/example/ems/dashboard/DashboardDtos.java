package com.example.ems.dashboard;

import java.util.List;

public class DashboardDtos {
    public record AdminDashboard(long totalEmployees, long activeEmployees, long departments, long pendingLeaveRequests) {
    }

    public record ManagerDashboard(long teamMembers, long pendingApprovals) {
    }

    public record EmployeeDashboard(List<?> leaveBalance, String attendanceSummary, List<String> upcomingHolidays) {
    }
}
