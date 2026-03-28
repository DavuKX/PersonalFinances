package com.personalfinance.transactionservice.presentation.controller;

import com.personalfinance.transactionservice.application.dto.CategoryDto;
import com.personalfinance.transactionservice.application.dto.CreateCategoryCommand;
import com.personalfinance.transactionservice.application.usecase.CategoryUseCase;
import com.personalfinance.transactionservice.domain.model.TransactionType;
import com.personalfinance.transactionservice.presentation.request.CreateCategoryRequest;
import com.personalfinance.transactionservice.presentation.response.CategoryResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/categories")
public class CategoryController {

    private final CategoryUseCase categoryUseCase;

    public CategoryController(CategoryUseCase categoryUseCase) {
        this.categoryUseCase = categoryUseCase;
    }

    @PostMapping
    public ResponseEntity<CategoryResponse> createCategory(
            @RequestHeader("X-User-Id") UUID userId,
            @RequestBody @Valid CreateCategoryRequest request) {
        CategoryDto dto = categoryUseCase.create(userId, new CreateCategoryCommand(
                request.getName(), request.getParentId(), request.getTransactionType()));
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(dto));
    }

    @GetMapping
    public ResponseEntity<List<CategoryResponse>> listCategories(
            @RequestHeader("X-User-Id") UUID userId,
            @RequestParam(required = false) TransactionType transactionType) {
        List<CategoryDto> dtos = categoryUseCase.listAccessible(userId, transactionType);
        return ResponseEntity.ok(dtos.stream().map(this::toResponse).toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<CategoryResponse> getCategory(
            @RequestHeader("X-User-Id") UUID userId,
            @PathVariable UUID id) {
        return ResponseEntity.ok(toResponse(categoryUseCase.getById(userId, id)));
    }

    @GetMapping("/{id}/subcategories")
    public ResponseEntity<List<CategoryResponse>> getSubcategories(
            @RequestHeader("X-User-Id") UUID userId,
            @PathVariable UUID id) {
        List<CategoryDto> dtos = categoryUseCase.listSubcategories(userId, id);
        return ResponseEntity.ok(dtos.stream().map(this::toResponse).toList());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCategory(
            @RequestHeader("X-User-Id") UUID userId,
            @PathVariable UUID id) {
        categoryUseCase.delete(userId, id);
        return ResponseEntity.noContent().build();
    }

    private CategoryResponse toResponse(CategoryDto dto) {
        CategoryResponse response = new CategoryResponse();
        response.setId(dto.id());
        response.setUserId(dto.userId());
        response.setName(dto.name());
        response.setParentId(dto.parentId());
        response.setTransactionType(dto.transactionType());
        response.setDefault(dto.isDefault());
        response.setCreatedAt(dto.createdAt());
        return response;
    }
}

