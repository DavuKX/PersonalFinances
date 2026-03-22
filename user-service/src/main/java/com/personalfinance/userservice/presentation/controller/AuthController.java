package com.personalfinance.userservice.presentation.controller;

import com.personalfinance.userservice.application.dto.LoginCommand;
import com.personalfinance.userservice.application.dto.TokenPair;
import com.personalfinance.userservice.application.usecase.AuthUseCase;
import com.personalfinance.userservice.domain.model.User;
import com.personalfinance.userservice.presentation.request.LoginRequest;
import com.personalfinance.userservice.presentation.request.RefreshRequest;
import com.personalfinance.userservice.presentation.response.LoginResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthUseCase authUseCase;

    public AuthController(AuthUseCase authUseCase) {
        this.authUseCase = authUseCase;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody @Valid LoginRequest request) {
        TokenPair pair = authUseCase.login(new LoginCommand(request.getEmail(), request.getPassword()));
        return ResponseEntity.ok(toLoginResponse(pair));
    }

    @PostMapping("/refresh")
    public ResponseEntity<LoginResponse> refresh(@RequestBody @Valid RefreshRequest request) {
        TokenPair pair = authUseCase.refresh(request.getRefreshToken());
        return ResponseEntity.ok(toLoginResponse(pair));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @AuthenticationPrincipal User user,
            @RequestHeader("X-Token-Jti") String jti) {
        authUseCase.logout(user.getId(), jti);
        return ResponseEntity.noContent().build();
    }

    private LoginResponse toLoginResponse(TokenPair pair) {
        return new LoginResponse(pair.accessToken(), pair.refreshToken(), pair.expiresInSeconds());
    }
}
