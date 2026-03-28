package com.personalfinance.transactionservice.application.usecase;

import com.personalfinance.transactionservice.application.dto.CategoryDto;
import com.personalfinance.transactionservice.application.dto.CreateCategoryCommand;
import com.personalfinance.transactionservice.domain.model.TransactionType;

import java.util.List;
import java.util.UUID;

public interface CategoryUseCase {
    CategoryDto create(UUID userId, CreateCategoryCommand command);
    CategoryDto getById(UUID userId, UUID categoryId);
    List<CategoryDto> listAccessible(UUID userId, TransactionType transactionType);
    List<CategoryDto> listSubcategories(UUID userId, UUID parentId);
    void delete(UUID userId, UUID categoryId);
}

