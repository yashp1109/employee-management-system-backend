package com.example.ems.request;

import com.example.ems.common.BadRequestException;
import com.example.ems.common.ResourceNotFoundException;
import com.example.ems.common.Role;
import com.example.ems.employee.Employee;
import com.example.ems.employee.EmployeeRepository;
import com.example.ems.department.DepartmentService;
import com.example.ems.designation.DesignationService;
import com.example.ems.security.CurrentUserService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Map;

import static com.example.ems.request.RequestDtos.*;

@Service
@RequiredArgsConstructor
public class RequestService {
    private final RequestRepository repository;
    private final EmployeeRepository employeeRepository;
    private final CurrentUserService currentUserService;
    private final DepartmentService departmentService;
    private final DesignationService designationService;
    private final ObjectMapper objectMapper;

    @Transactional
    public RequestDto createProfileUpdate(ProfileUpdateRequestDto request) {
        Employee currentUser = currentUserService.employee();
        Employee targetEmployee = employeeRepository.findById(request.employeeId())
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found"));
        Request entity = new Request();
        entity.setType(RequestType.PROFILE_UPDATE);
        entity.setEmployee(targetEmployee);
        entity.setRequestedBy(currentUser);
        entity.setComment(request.comment());
        if (request.payload() != null && !request.payload().isEmpty()) {
            entity.setPayload(writePayload(request.payload()));
        }
        return toDto(repository.save(entity));
    }

    @Transactional(readOnly = true)
    public Page<RequestDto> search(Long employeeId, RequestType type, RequestStatus status, Pageable pageable) {
        Employee currentUser = currentUserService.employee();
        if (currentUser.getRole() == Role.ADMIN || currentUser.getRole() == Role.HR) {
            return repository.search(employeeId, type, status, pageable).map(this::toDto);
        }
        if (employeeId != null && !employeeId.equals(currentUser.getId())) {
            throw new AccessDeniedException("Not authorized to view these requests");
        }
        return repository.searchByRequester(currentUser, employeeId, type, status, pageable).map(this::toDto);
    }

    @Transactional
    public RequestDto action(Long id, RequestActionDto action) {
        Request request = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Request not found"));
        Employee currentUser = currentUserService.employee();
        if (currentUser.getRole() != Role.ADMIN && currentUser.getRole() != Role.HR) {
            throw new AccessDeniedException("Only ADMIN or HR can take action on requests");
        }
        if (request.getStatus() != RequestStatus.PENDING) {
            throw new BadRequestException("Only pending requests can be actioned");
        }
        request.setReviewer(currentUser);
        request.setReviewerComment(action.reviewerComment());
        if ("APPROVE".equalsIgnoreCase(action.action())) {
            request.setStatus(RequestStatus.APPROVED);
            applyProfileChange(request);
        } else if ("REJECT".equalsIgnoreCase(action.action())) {
            request.setStatus(RequestStatus.REJECTED);
        } else {
            throw new BadRequestException("Action must be APPROVE or REJECT");
        }
        return toDto(request);
    }

    private void applyProfileChange(Request request) {
        if (request.getType() != RequestType.PROFILE_UPDATE || request.getPayload() == null) {
            return;
        }
        Map<String, Object> payload = readPayload(request.getPayload());
        if (payload.isEmpty()) {
            return;
        }
        Employee employee = request.getEmployee();
        if (payload.containsKey("firstName")) {
            employee.setFirstName(payload.get("firstName").toString());
        }
        if (payload.containsKey("lastName")) {
            employee.setLastName(payload.get("lastName").toString());
        }
        if (payload.containsKey("email")) {
            employee.setEmail(payload.get("email").toString());
        }
        if (payload.containsKey("mobileNumber")) {
            employee.setMobileNumber(payload.get("mobileNumber").toString());
        }
        if (payload.containsKey("gender")) {
            employee.setGender(com.example.ems.common.Gender.valueOf(payload.get("gender").toString()));
        }
        if (payload.containsKey("dateOfBirth")) {
            employee.setDateOfBirth(java.time.LocalDate.parse(payload.get("dateOfBirth").toString()));
        }
        if (payload.containsKey("dateOfJoining")) {
            employee.setDateOfJoining(java.time.LocalDate.parse(payload.get("dateOfJoining").toString()));
        }
        if (payload.containsKey("departmentId")) {
            Long departmentId = Long.valueOf(payload.get("departmentId").toString());
            employee.setDepartment(departmentService.get(departmentId));
        }
        if (payload.containsKey("designationId")) {
            Long designationId = Long.valueOf(payload.get("designationId").toString());
            employee.setDesignation(designationService.get(designationId));
        }
        if (payload.containsKey("managerId")) {
            Object managerObject = payload.get("managerId");
            if (managerObject == null) {
                employee.setManager(null);
            } else {
                Long managerId = Long.valueOf(managerObject.toString());
                employee.setManager(employeeRepository.findById(managerId)
                        .orElseThrow(() -> new ResourceNotFoundException("Manager not found")));
            }
        }
        if (payload.containsKey("salary")) {
            employee.setSalary(new BigDecimal(payload.get("salary").toString()));
        }
        if (payload.containsKey("status")) {
            employee.setStatus(com.example.ems.common.Status.valueOf(payload.get("status").toString()));
        }
        if (payload.containsKey("role")) {
            employee.setRole(com.example.ems.common.Role.valueOf(payload.get("role").toString()));
        }
    }

    private RequestDto toDto(Request request) {
        return new RequestDto(
                request.getId(),
                request.getType(),
                request.getEmployee().getId(),
                request.getEmployee().getFirstName() + " " + request.getEmployee().getLastName(),
                request.getRequestedBy().getId(),
                request.getRequestedBy().getFirstName() + " " + request.getRequestedBy().getLastName(),
                request.getComment(),
                request.getStatus(),
                request.getReviewer() == null ? null : request.getReviewer().getId(),
                request.getReviewer() == null ? null : request.getReviewer().getFirstName() + " " + request.getReviewer().getLastName(),
                request.getReviewerComment(),
                request.getCreatedAt(),
                request.getUpdatedAt(),
                request.getPayload() == null ? null : readPayload(request.getPayload())
        );
    }

    private Map<String, Object> readPayload(String payload) {
        try {
            return objectMapper.readValue(payload, new TypeReference<>() {
            });
        } catch (Exception e) {
            throw new BadRequestException("Invalid request payload");
        }
    }

    private String writePayload(Map<String, Object> payload) {
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (Exception e) {
            throw new BadRequestException("Invalid request payload");
        }
    }
}
