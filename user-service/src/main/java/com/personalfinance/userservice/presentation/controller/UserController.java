package com.personalfinance.userservice.presentation.controller;

import com.personalfinance.userservice.application.dto.RegisterCommand;
import com.personalfinance.userservice.application.dto.UserDto;
import com.personalfinance.userservice.application.usecase.UserUseCase;
import com.personalfinance.userservice.domain.model.User;
import com.personalfinance.userservice.presentation.request.RegisterRequest;
import com.personalfinance.userservice.presentation.response.UserResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserUseCase userUseCase;

    public UserController(UserUseCase userUseCase) {
        this.userUseCase = userUseCase;
    }

    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@RequestBody @Valid RegisterRequest request) {
        UserDto dto = userUseCase.register(new RegisterCommand(request.getUsername(), request.getEmail(), request.getPassword()));
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(dto));
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponse> me(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(toResponse(userUseCase.findById(user.getId())));
    }

    @GetMapping("/health")
    public String health() {
        return "OK";
    }

    private UserResponse toResponse(UserDto dto) {
        UserResponse response = new UserResponse();
        response.setId(dto.id());
        response.setUsername(dto.username());
        response.setEmail(dto.email());
        response.setRoles(dto.roles());
        response.setCreatedAt(dto.createdAt());
        return response;
    }
}
