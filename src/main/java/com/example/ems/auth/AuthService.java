package com.example.ems.auth;

import com.example.ems.common.BadRequestException;
import com.example.ems.employee.Employee;
import com.example.ems.employee.EmployeeRepository;
import com.example.ems.security.CurrentUserService;
import com.example.ems.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.example.ems.auth.AuthDtos.*;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final AuthenticationManager authenticationManager;
    private final EmployeeRepository employeeRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final CurrentUserService currentUserService;

    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.email(), request.password()));
        Employee employee = employeeRepository.findByEmailIgnoreCase(request.email())
                .orElseThrow(() -> new BadRequestException("Invalid credentials"));
        return response(employee);
    }

    public AuthResponse refresh(String refreshToken) {
        if (!jwtService.isValid(refreshToken) || !"refresh".equals(jwtService.tokenType(refreshToken))) {
            throw new BadRequestException("Invalid refresh token");
        }
        Employee employee = employeeRepository.findByEmailIgnoreCase(jwtService.subject(refreshToken))
                .orElseThrow(() -> new BadRequestException("Invalid refresh token"));
        return response(employee);
    }

    @Transactional
    public void changePassword(ChangePasswordRequest request) {
        Employee employee = currentUserService.employee();
        if (!passwordEncoder.matches(request.currentPassword(), employee.getPasswordHash())) {
            throw new BadRequestException("Current password is incorrect");
        }
        employee.setPasswordHash(passwordEncoder.encode(request.newPassword()));
    }

    public void forgotPassword(ForgotPasswordRequest request) {
        // Production systems should enqueue a reset email with a short-lived token.
        employeeRepository.findByEmailIgnoreCase(request.email()).ifPresent(employee -> {});
    }

    private AuthResponse response(Employee employee) {
        return new AuthResponse(
                jwtService.createAccessToken(employee),
                jwtService.createRefreshToken(employee),
                employee.getId(),
                employee.getFirstName() + " " + employee.getLastName(),
                employee.getRole(),
                employee.getDepartment() == null ? null : employee.getDepartment().getId()
        );
    }
}
