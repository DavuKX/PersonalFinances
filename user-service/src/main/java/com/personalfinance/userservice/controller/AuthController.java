package com.personalfinance.userservice.controller;

import com.personalfinance.userservice.dto.LoginRequest;
import com.personalfinance.userservice.dto.LoginResponse;
import com.personalfinance.userservice.dto.RefreshRequest;
import com.personalfinance.userservice.entity.User;
import com.personalfinance.userservice.service.AuthService;
import com.personalfinance.userservice.service.RefreshTokenService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {
    private final AuthService authService;
    private final RefreshTokenService refreshTokenService;

    public AuthController(AuthService authService, RefreshTokenService refreshTokenService) {
        this.authService = authService;
        this.refreshTokenService = refreshTokenService;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody @Valid LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/refresh")
    public ResponseEntity<LoginResponse> refresh(@RequestBody @Valid RefreshRequest request) {
        return ResponseEntity.ok(authService.refresh(request.getRefreshToken()));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @AuthenticationPrincipal User user,
            @RequestHeader("X-Token-Jti") String jti) {
        authService.logout(user, jti);
        return ResponseEntity.noContent().build();
    }
}
