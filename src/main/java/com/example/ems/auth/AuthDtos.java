package com.example.ems.auth;

import com.example.ems.common.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class AuthDtos {
    public record LoginRequest(@Email @NotBlank String email, @NotBlank String password) {
    }

    public record AuthResponse(String accessToken, String refreshToken, Long employeeId, String fullName, Role role, Long departmentId) {
    }

    public record RefreshRequest(@NotBlank String refreshToken) {
    }

    public record ChangePasswordRequest(@NotBlank String currentPassword, @Size(min = 8, max = 72) String newPassword) {
    }

    public record ForgotPasswordRequest(@Email @NotBlank String email) {
    }

    public record MessageResponse(String message) {
    }
}
