package com.example.ems.auth;

import com.example.ems.common.ApiResonse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import static com.example.ems.auth.AuthDtos.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping("/login")
    AuthResponse login(@RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @PostMapping("/refresh")
    AuthResponse refresh(@RequestBody RefreshRequest request) {
        return authService.refresh(request.refreshToken());
    }

    @PostMapping("/change-password")
    MessageResponse changePassword(@RequestBody ChangePasswordRequest request) {
        authService.changePassword(request);
        return new MessageResponse("Password changed successfully");
    }

    @PostMapping("/forgot-password")
    MessageResponse forgotPassword(@RequestBody ForgotPasswordRequest request) {
        authService.forgotPassword(request);
        return new MessageResponse("If the email exists, password reset instructions will be sent");
    }

    @PostMapping("/logout")
    MessageResponse logout() {
        return new MessageResponse("Logged out");
    }
}
