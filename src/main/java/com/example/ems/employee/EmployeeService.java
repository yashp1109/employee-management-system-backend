package com.example.ems.employee;

import com.example.ems.common.BadRequestException;
import com.example.ems.common.ResourceNotFoundException;
import com.example.ems.common.Role;
import com.example.ems.department.DepartmentService;
import com.example.ems.designation.DesignationService;
import com.example.ems.security.CurrentUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EmployeeService {
    private final EmployeeRepository repository;
    private final DepartmentService departmentService;
    private final DesignationService designationService;
    private final PasswordEncoder passwordEncoder;
    private final CurrentUserService currentUserService;

    @Transactional(readOnly = true)
    public Page<EmployeeDto> search(String q, Long departmentId, Long managerId,
                                    LocalDate joiningFrom, LocalDate joiningTo, Pageable pageable) {
        Employee currentUser = currentUserService.employee();
        Specification<Employee> spec = Specification.where(visibilitySpec(currentUser))
                .and(querySpec(q))
                .and(departmentId == null ? null
                        : (root, cq, cb) -> cb.equal(root.get("department").get("id"), departmentId))
                .and(managerId == null ? null
                        : (root, cq, cb) -> cb.equal(root.get("manager").get("id"), managerId))
                .and(joiningFrom == null ? null
                        : (root, cq, cb) -> cb.greaterThanOrEqualTo(root.get("dateOfJoining"), joiningFrom))
                .and(joiningTo == null ? null
                        : (root, cq, cb) -> cb.lessThanOrEqualTo(root.get("dateOfJoining"), joiningTo));
        return repository.findAll(spec, pageable).map(this::toDto);
    }

    @Transactional(readOnly = true)
    public List<ManagerDto> managers() {
        return repository.findDistinctManagers().stream()
                .map(m -> new ManagerDto(m.getId(), m.getFirstName() + " " + m.getLastName()))
                .toList();
    }

    private Specification<Employee> visibilitySpec(Employee currentUser) {
        if (currentUser.getRole() == Role.ADMIN || currentUser.getRole() == Role.HR) {
            return null;
        }
        if (currentUser.getRole() == Role.MANAGER && currentUser.getDepartment() != null) {
            Long deptId = currentUser.getDepartment().getId();
            return (root, cq, cb) -> cb.equal(root.get("department").get("id"), deptId);
        }
        Long selfId = currentUser.getId();
        return (root, cq, cb) -> cb.equal(root.get("id"), selfId);
    }

    private Specification<Employee> querySpec(String q) {
        if (!StringUtils.hasText(q)) {
            return null;
        }
        String like = "%" + q.trim().toLowerCase() + "%";
        return (root, cq, cb) -> {
            var department = root.join("department", jakarta.persistence.criteria.JoinType.LEFT);
            var designation = root.join("designation", jakarta.persistence.criteria.JoinType.LEFT);
            return cb.or(
                    cb.like(cb.lower(root.get("firstName")), like),
                    cb.like(cb.lower(root.get("lastName")), like),
                    cb.like(cb.lower(root.get("email")), like),
                    cb.like(cb.lower(root.get("employeeCode")), like),
                    cb.like(cb.lower(cb.coalesce(department.get("name"), "")), like),
                    cb.like(cb.lower(cb.coalesce(designation.get("name"), "")), like)
            );
        };
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
        employee.setDesignation(request.designationId() == null ? null : designationService.get(request.designationId()));
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
