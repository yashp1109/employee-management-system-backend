package com.example.ems.security;

import com.example.ems.common.ResourceNotFoundException;
import com.example.ems.employee.Employee;
import com.example.ems.employee.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CurrentUserService {
    private final EmployeeRepository employeeRepository;

    public Employee employee() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return employeeRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ResourceNotFoundException("Current user not found"));
    }
}
