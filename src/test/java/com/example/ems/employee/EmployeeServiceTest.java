//package com.example.ems.employee;
//
//import com.example.ems.common.Role;
//import com.example.ems.department.DepartmentService;
//import com.example.ems.designation.DesignationService;
//import com.example.ems.security.CurrentUserService;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//
//import java.util.Optional;
//
//import static org.junit.jupiter.api.Assertions.assertThrows;
//import static org.mockito.Mockito.when;
//
//@org.junit.jupiter.api.extension.ExtendWith(MockitoExtension.class)
//class EmployeeServiceTest {
//    @Mock
//    private EmployeeRepository repository;
//    @Mock
//    private DepartmentService departmentService;
//    @Mock
//    private DesignationService designationService;
//    @Mock
//    private CurrentUserService currentUserService;
//    @Mock
//    private org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;
//
//    private EmployeeService service;
//
////    @BeforeEach
////    void setUp() {
////        service = new EmployeeService(repository, departmentService, designationService, passwordEncoder, currentUserService);
////    }
//
//    @Test
//    void getDto_shouldDenyWhenNotSameDepartmentAndNotAdminHr() {
//        Employee currentUser = new Employee();
//        currentUser.setId(5L);
//        currentUser.setRole(Role.EMPLOYEE);
//        Employee other = new Employee();
//        other.setId(32L);
//        other.setRole(Role.EMPLOYEE);
//        when(currentUserService.employee()).thenReturn(currentUser);
//        when(repository.findById(32L)).thenReturn(Optional.of(other));
//
//        assertThrows(org.springframework.security.access.AccessDeniedException.class,
//                () -> service.getDto(32L));
//    }
//}
