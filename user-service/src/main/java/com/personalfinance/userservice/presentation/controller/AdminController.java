package com.personalfinance.userservice.presentation.controller;

import com.personalfinance.userservice.application.dto.UpdateRolesCommand;
import com.personalfinance.userservice.application.dto.UserDto;
import com.personalfinance.userservice.application.dto.UserPageDto;
import com.personalfinance.userservice.application.usecase.AdminUseCase;
import com.personalfinance.userservice.presentation.request.UpdateRolesRequest;
import com.personalfinance.userservice.presentation.response.UserPageResponse;
import com.personalfinance.userservice.presentation.response.UserResponse;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/users")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AdminUseCase adminUseCase;

    public AdminController(AdminUseCase adminUseCase) {
        this.adminUseCase = adminUseCase;
    }

    @GetMapping
    public ResponseEntity<UserPageResponse> listUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String direction) {
        Sort sort = direction.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        UserPageDto pageDto = adminUseCase.listUsers(PageRequest.of(page, size, sort));
        return ResponseEntity.ok(toPageResponse(pageDto));
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUser(@PathVariable UUID id) {
        return ResponseEntity.ok(toResponse(adminUseCase.getUser(id)));
    }

    @PutMapping("/{id}/roles")
    public ResponseEntity<UserResponse> updateRoles(
            @PathVariable UUID id,
            @RequestBody @Valid UpdateRolesRequest request) {
        return ResponseEntity.ok(toResponse(adminUseCase.updateRoles(id, new UpdateRolesCommand(request.getRoles()))));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable UUID id) {
        adminUseCase.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    private UserPageResponse toPageResponse(UserPageDto dto) {
        List<UserResponse> content = dto.content().stream().map(this::toResponse).toList();
        return new UserPageResponse(content, dto.page(), dto.size(), dto.totalElements(), dto.totalPages());
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
