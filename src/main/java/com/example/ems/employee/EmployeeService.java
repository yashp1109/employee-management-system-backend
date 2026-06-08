package com.example.ems.employee;

import com.example.ems.common.BadRequestException;
import com.example.ems.common.ResourceNotFoundException;
import com.example.ems.common.Role;
import com.example.ems.department.DepartmentService;
import com.example.ems.designation.DesignationService;
import com.example.ems.security.CurrentUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class EmployeeService {
    private final EmployeeRepository repository;
    private final DepartmentService departmentService;
    private final DesignationService designationService;
    private final PasswordEncoder passwordEncoder;
    private final CurrentUserService currentUserService;

    @Transactional(readOnly = true)
    public Page<EmployeeDto> search(String q, Pageable pageable) {
        Employee currentUser = currentUserService.employee();
        boolean isAdminOrHr = currentUser.getRole() == Role.ADMIN || currentUser.getRole() == Role.HR;
        if (isAdminOrHr) {
            Page<Employee> employees = StringUtils.hasText(q)
                    ? repository.search(q, pageable)
                    : repository.findAll(pageable);
            return employees.map(this::toDto);
        }
        if (currentUser.getRole() == Role.MANAGER && currentUser.getDepartment() != null) {
            if (StringUtils.hasText(q)) {
                Page<Employee> employees = repository.search(q, Pageable.unpaged());
                var filtered = employees.stream()
                        .filter(e -> e.getDepartment() != null && currentUser.getDepartment().getId().equals(e.getDepartment().getId()))
                        .toList();
                return new PageImpl<>(filtered, pageable, filtered.size()).map(this::toDto);
            }
            return repository.findByDepartmentId(currentUser.getDepartment().getId(), pageable).map(this::toDto);
        }
        if (StringUtils.hasText(q) && !matchesQuery(currentUser, q)) {
            return Page.empty(pageable);
        }
        return new PageImpl<>(java.util.List.of(currentUser), pageable, 1).map(this::toDto);
    }

    @Transactional(readOnly = true)
    public EmployeeDto getDto(Long id) {
        Employee currentUser = currentUserService.employee();
        Employee employee = get(id);
        if (canViewEmployee(currentUser, employee)) {
            return toDto(employee);
        }
        throw new AccessDeniedException("Not authorized to view this employee");
    }

    @Transactional(readOnly = true)
    public EmployeeDto getMe() {
        return toDto(currentUserService.employee());
    }

    @Transactional
    public EmployeeDto create(EmployeeRequest request) {
        if (repository.existsByEmailIgnoreCase(request.email())) {
            throw new BadRequestException("Email already exists");
        }
        if (repository.existsByEmployeeCodeIgnoreCase(request.employeeCode())) {
            throw new BadRequestException("Employee code already exists");
        }
        Employee employee = new Employee();
        apply(request, employee);
        employee.setPasswordHash(passwordEncoder.encode(StringUtils.hasText(request.password()) ? request.password() : "Password@123"));
        return toDto(repository.save(employee));
    }

    @Transactional
    public EmployeeDto update(Long id, EmployeeRequest request) {
        Employee employee = get(id);
        apply(request, employee);
        if (StringUtils.hasText(request.password())) {
            employee.setPasswordHash(passwordEncoder.encode(request.password()));
        }
        return toDto(employee);
    }

    @Transactional
    public void delete(Long id) {
        repository.delete(get(id));
    }

    public Employee get(Long id) {
        return repository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Employee not found"));
    }

    private void apply(EmployeeRequest request, Employee employee) {
        employee.setEmployeeCode(request.employeeCode());
        employee.setFirstName(request.firstName());
        employee.setLastName(request.lastName());
        employee.setEmail(request.email());
        employee.setMobileNumber(request.mobileNumber());
        employee.setGender(request.gender());
        employee.setDateOfBirth(request.dateOfBirth());
        employee.setDateOfJoining(request.dateOfJoining());
        employee.setDepartment(departmentService.get(request.departmentId()));
        employee.setDesignation(designationService.get(request.designationId()));
        employee.setSalary(request.salary());
        employee.setManager(request.managerId() == null ? null : get(request.managerId()));
        employee.setStatus(request.status());
        employee.setRole(request.role());
    }

    private boolean canViewEmployee(Employee currentUser, Employee employee) {
        if (currentUser.getRole() == Role.ADMIN || currentUser.getRole() == Role.HR) {
            return true;
        }
        if (currentUser.getId().equals(employee.getId())) {
            return true;
        }
        return currentUser.getRole() == Role.MANAGER
                && currentUser.getDepartment() != null
                && employee.getDepartment() != null
                && currentUser.getDepartment().getId().equals(employee.getDepartment().getId());
    }

    private boolean matchesQuery(Employee employee, String query) {
        String normalized = query.trim().toLowerCase();
        return employee.getFirstName().toLowerCase().contains(normalized)
                || employee.getLastName().toLowerCase().contains(normalized)
                || employee.getEmail().toLowerCase().contains(normalized)
                || employee.getEmployeeCode().toLowerCase().contains(normalized)
                || (employee.getDepartment() != null && employee.getDepartment().getName().toLowerCase().contains(normalized));
    }

    @Transactional(value = "true")
    private EmployeeDto toDto(Employee employee) {
        Employee manager = employee.getManager();
        return EmployeeDto.builder()
                .id(employee.getId())
                .employeeCode(employee.getEmployeeCode())
                .firstName(employee.getFirstName())
                .lastName(employee.getLastName())
                .email(employee.getEmail())
                .mobileNumber(employee.getMobileNumber())
                .gender(employee.getGender())
                .dateOfBirth(employee.getDateOfBirth())
                .dateOfJoining(employee.getDateOfJoining())
                .departmentId(employee.getDepartment() == null ? null : employee.getDepartment().getId())
                .departmentName(employee.getDepartment() == null ? null : employee.getDepartment().getName())
                .designationId(employee.getDesignation() == null ? null : employee.getDesignation().getId())
                .designationName(employee.getDesignation() == null ? null : employee.getDesignation().getName())
                .salary(employee.getSalary())
                .managerId(manager == null ? null : manager.getId())
                .managerName(manager == null ? null : manager.getFirstName() + " " + manager.getLastName())
                .status(employee.getStatus())
                .role(employee.getRole())
                .build();
    }
}
