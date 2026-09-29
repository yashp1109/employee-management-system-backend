//package com.example.ems.request;
//
//import com.example.ems.common.Role;
//import com.example.ems.employee.Employee;
//import com.example.ems.employee.EmployeeRepository;
//import com.example.ems.department.DepartmentService;
//import com.example.ems.designation.DesignationService;
//import com.example.ems.security.CurrentUserService;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.mockito.ArgumentCaptor;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//import org.springframework.data.domain.PageImpl;
//
//import java.util.List;
//import java.util.Map;
//import java.util.Optional;
//
//import static org.junit.jupiter.api.Assertions.*;
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.Mockito.*;
//
//@org.junit.jupiter.api.extension.ExtendWith(MockitoExtension.class)
//class RequestServiceTest {
//    @Mock
//    private RequestRepository repository;
//    @Mock
//    private EmployeeRepository employeeRepository;
//    @Mock
//    private CurrentUserService currentUserService;
//    @Mock
//    private DepartmentService departmentService;
//    @Mock
//    private DesignationService designationService;
//
//    private RequestService service;
//
//    @BeforeEach
//    void setUp() {
//        service = new RequestService(repository, employeeRepository, currentUserService, departmentService, designationService, new ObjectMapper());
//    }
//
//    @Test
//    void approveProfileUpdateRequest_shouldSetRequestApprovedAndApplyPayload() {
//        Employee hr = new Employee();
//        hr.setId(10L);
//        hr.setRole(Role.HR);
//        hr.setFirstName("Helen");
//        hr.setLastName("Riley");
//
//        Employee employee = new Employee();
//        employee.setId(101L);
//        employee.setFirstName("Alice");
//        employee.setLastName("A");
//
//        Request request = new Request();
//        request.setId(1L);
//        request.setType(RequestType.PROFILE_UPDATE);
//        request.setEmployee(employee);
//        request.setRequestedBy(hr);
//        request.setPayload("{\"firstName\":\"Ally\",\"lastName\":\"Anderson\"}");
//        request.setStatus(RequestStatus.PENDING);
//
//        when(currentUserService.employee()).thenReturn(hr);
//        when(repository.findById(1L)).thenReturn(Optional.of(request));
//
//        RequestDto result = service.action(1L, new RequestActionDto("APPROVE", "Approved"));
//
//        assertEquals(RequestStatus.APPROVED, result.status());
//        assertEquals("Ally", employee.getFirstName());
//        assertEquals("Anderson", employee.getLastName());
//        assertEquals(1L, result.id());
//        assertEquals("Approved", result.reviewerComment());
//        assertEquals(hr.getId(), result.reviewerId());
//    }
//
//    @Test
//    void search_shouldDenyOtherEmployeeRequestsForNonAdmin() {
//        Employee user = new Employee();
//        user.setId(25L);
//        user.setRole(Role.EMPLOYEE);
//        when(currentUserService.employee()).thenReturn(user);
//
//        assertThrows(org.springframework.security.access.AccessDeniedException.class,
//                () -> service.search(42L, null, null, org.springframework.data.domain.Pageable.unpaged()));
//    }
//}
