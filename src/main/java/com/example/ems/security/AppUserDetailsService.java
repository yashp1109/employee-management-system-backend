package com.example.ems.security;

import com.example.ems.common.Status;
import com.example.ems.employee.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AppUserDetailsService implements UserDetailsService {
    private final EmployeeRepository employeeRepository;

    @Override
    public UserDetails loadUserByUsername(String username) {
        return employeeRepository.findByEmailIgnoreCase(username)
                .map(employee -> new User(
                        employee.getEmail(),
                        employee.getPasswordHash(),
                        employee.getStatus() == Status.ACTIVE,
                        true,
                        true,
                        true,
                        List.of(new SimpleGrantedAuthority("ROLE_" + employee.getRole().name()))
                ))
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }
}
