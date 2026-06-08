package com.example.ems.leave;

import com.example.ems.common.BadRequestException;
import com.example.ems.common.ResourceNotFoundException;
import com.example.ems.common.Role;
import com.example.ems.employee.Employee;
import com.example.ems.security.CurrentUserService;
import com.example.ems.employee.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static com.example.ems.leave.LeaveDtos.*;

@Service
@RequiredArgsConstructor
public class LeaveService {
    private final LeaveRequestRepository leaveRequestRepository;
    private final LeaveTypeRepository leaveTypeRepository;
    private final LeaveBalanceRepository leaveBalanceRepository;
    private final CurrentUserService currentUserService;
    private final EmployeeRepository employeeRepository;

    @Transactional
    public LeaveRequestDto apply(ApplyLeaveRequest request) {
        Employee employee = currentUserService.employee();
        LeaveType type = leaveTypeRepository.findById(request.leaveTypeId())
                .orElseThrow(() -> new ResourceNotFoundException("Leave type not found"));
        if (request.endDate().isBefore(request.startDate())) {
            throw new BadRequestException("End date cannot be before start date");
        }
        int days = (int) ChronoUnit.DAYS.between(request.startDate(), request.endDate()) + 1;
        LeaveBalance balance = leaveBalanceRepository.findByEmployeeAndLeaveTypeAndYear(employee, type, request.startDate().getYear())
                .orElseThrow(() -> new BadRequestException("Leave balance is not configured"));
        if (balance.getRemainingDays() < days) {
            throw new BadRequestException("Insufficient leave balance");
        }
        LeaveRequest leave = new LeaveRequest();
        leave.setEmployee(employee);
        leave.setLeaveType(type);
        leave.setStartDate(request.startDate());
        leave.setEndDate(request.endDate());
        leave.setDays(days);
        leave.setReason(request.reason());
        return toDto(leaveRequestRepository.save(leave));
    }

    @Transactional(readOnly = true)
    public Page<LeaveRequestDto> myHistory(Pageable pageable) {
        return leaveRequestRepository.findByEmployee(currentUserService.employee(), pageable).map(this::toDto);
    }

    @Transactional(readOnly = true)
    public Page<LeaveRequestDto> list(Long employeeId, Pageable pageable) {
        Employee currentUser = currentUserService.employee();
        if (employeeId == null) {
            if (currentUser.getRole() == Role.ADMIN || currentUser.getRole() == Role.HR) {
                return leaveRequestRepository.findAll(pageable).map(this::toDto);
            }
            return myHistory(pageable);
        }
        if (currentUser.getRole() == Role.ADMIN || currentUser.getRole() == Role.HR) {
            return leaveRequestRepository.findByEmployeeId(employeeId, pageable).map(this::toDto);
        }
        if (currentUser.getId().equals(employeeId)) {
            return leaveRequestRepository.findByEmployee(currentUser, pageable).map(this::toDto);
        }
        Employee target = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new BadRequestException("Employee not found"));
        if (currentUser.getRole() == Role.MANAGER
                && currentUser.getDepartment() != null
                && target.getDepartment() != null
                && currentUser.getDepartment().getId().equals(target.getDepartment().getId())) {
            return leaveRequestRepository.findByEmployeeId(employeeId, pageable).map(this::toDto);
        }
        throw new AccessDeniedException("Not authorized to view leaves for this employee");
    }

    @Transactional(readOnly = true)
    public Page<LeaveRequestDto> pendingApprovals(Pageable pageable) {
        Employee employee = currentUserService.employee();
        if (employee.getRole() == Role.ADMIN || employee.getRole() == Role.HR) {
            return leaveRequestRepository.findByStatus(LeaveStatus.PENDING, pageable).map(this::toDto);
        }
        return leaveRequestRepository.findByEmployeeManagerAndStatus(employee, LeaveStatus.PENDING, pageable).map(this::toDto);
    }

    @Transactional(readOnly = true)
    public long countPendingApprovals() {
        Employee employee = currentUserService.employee();
        if (employee.getRole() == Role.ADMIN || employee.getRole() == Role.HR) {
            return leaveRequestRepository.countByStatus(LeaveStatus.PENDING);
        }
        return leaveRequestRepository.findByEmployeeManagerAndStatus(employee, LeaveStatus.PENDING, Pageable.unpaged()).getTotalElements();
    }

    @Transactional(readOnly = true)
    public long countTeamMembers() {
        Employee employee = currentUserService.employee();
        if (employee.getDepartment() == null) {
            return 0;
        }
        return employeeRepository.countByDepartmentId(employee.getDepartment().getId());
    }

    @Transactional
    public LeaveRequestDto approve(Long id) {
        LeaveRequest leave = get(id);
        Employee current = currentUserService.employee();
        if (leave.getStatus() != LeaveStatus.PENDING) {
            throw new BadRequestException("Only pending leave can be approved");
        }
        if (!canApproveLeave(current, leave)) {
            throw new AccessDeniedException("Not authorized to approve this leave request");
        }
        LeaveBalance balance = leaveBalanceRepository.findByEmployeeAndLeaveTypeAndYear(
                        leave.getEmployee(), leave.getLeaveType(), leave.getStartDate().getYear())
                .orElseThrow(() -> new BadRequestException("Leave balance is not configured"));
        if (balance.getRemainingDays() < leave.getDays()) {
            throw new BadRequestException("Insufficient leave balance");
        }
        balance.setUsedDays(balance.getUsedDays() + leave.getDays());
        leave.setStatus(LeaveStatus.APPROVED);
        leave.setApprovedBy(current);
        leave.setApprovedAt(Instant.now());
        return toDto(leave);
    }

    @Transactional
    public LeaveRequestDto reject(Long id) {
        LeaveRequest leave = get(id);
        Employee current = currentUserService.employee();
        if (!canApproveLeave(current, leave)) {
            throw new AccessDeniedException("Not authorized to reject this leave request");
        }
        leave.setStatus(LeaveStatus.REJECTED);
        leave.setApprovedBy(current);
        leave.setApprovedAt(Instant.now());
        return toDto(leave);
    }

    private boolean canApproveLeave(Employee currentUser, LeaveRequest leave) {
        if (currentUser.getRole() == Role.ADMIN || currentUser.getRole() == Role.HR) {
            return true;
        }
        return currentUser.getRole() == Role.MANAGER
                && leave.getEmployee().getManager() != null
                && leave.getEmployee().getManager().getId().equals(currentUser.getId());
    }

    @Transactional
    public LeaveRequestDto cancel(Long id) {
        LeaveRequest leave = get(id);
        Employee current = currentUserService.employee();
        if (!leave.getEmployee().getId().equals(current.getId())) {
            throw new BadRequestException("Only the requester can cancel leave");
        }
        if (leave.getStatus() == LeaveStatus.APPROVED) {
            LeaveBalance balance = leaveBalanceRepository.findByEmployeeAndLeaveTypeAndYear(leave.getEmployee(), leave.getLeaveType(), leave.getStartDate().getYear())
                    .orElseThrow(() -> new BadRequestException("Leave balance is not configured"));
            balance.setUsedDays(Math.max(0, balance.getUsedDays() - leave.getDays()));
        }
        leave.setStatus(LeaveStatus.CANCELLED);
        return toDto(leave);
    }

    @Transactional(readOnly = true)
    public List<LeaveBalanceDto> balances() {
        Employee employee = currentUserService.employee();
        return leaveBalanceRepository.findByEmployeeAndYear(employee, java.time.LocalDate.now().getYear())
                .stream()
                .map(b -> new LeaveBalanceDto(b.getLeaveType().getId(), b.getLeaveType().getName(), b.getTotalDays(), b.getUsedDays(), b.getRemainingDays()))
                .toList();
    }

    private LeaveRequest get(Long id) {
        return leaveRequestRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Leave request not found"));
    }

    private LeaveRequestDto toDto(LeaveRequest l) {
        return new LeaveRequestDto(
                l.getId(),
                l.getEmployee().getId(),
                l.getEmployee().getFirstName() + " " + l.getEmployee().getLastName(),
                l.getLeaveType().getId(),
                l.getLeaveType().getName(),
                l.getStartDate(),
                l.getEndDate(),
                l.getDays(),
                l.getReason(),
                l.getStatus()
        );
    }
}
