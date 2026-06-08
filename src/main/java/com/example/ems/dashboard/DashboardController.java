package com.example.ems.dashboard;

import com.example.ems.common.Status;
import com.example.ems.department.DepartmentRepository;
import com.example.ems.employee.EmployeeRepository;
import com.example.ems.leave.LeaveService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static com.example.ems.dashboard.DashboardDtos.*;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {
    private final EmployeeRepository employeeRepository;
    private final DepartmentRepository departmentRepository;
    private final LeaveService leaveService;

    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    AdminDashboard admin() {
        return new AdminDashboard(
                employeeRepository.count(),
                employeeRepository.countByStatus(Status.ACTIVE),
                departmentRepository.count(),
                leaveService.countPendingApprovals()
        );
    }

    @GetMapping("/manager")
    @PreAuthorize("hasAnyRole('MANAGER','ADMIN','HR')")
    ManagerDashboard manager() {
        return new ManagerDashboard(leaveService.countTeamMembers(), leaveService.countPendingApprovals());
    }

    @GetMapping("/employee")
    EmployeeDashboard employee() {
        return new EmployeeDashboard(leaveService.balances(), "Current month attendance available from /api/attendance", List.of("Republic Day", "Independence Day"));
    }
}
