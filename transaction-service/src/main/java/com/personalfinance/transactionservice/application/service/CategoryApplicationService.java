package com.personalfinance.transactionservice.application.service;

import com.personalfinance.transactionservice.application.dto.CategoryDto;
import com.personalfinance.transactionservice.application.dto.CreateCategoryCommand;
import com.personalfinance.transactionservice.application.usecase.CategoryUseCase;
import com.personalfinance.transactionservice.domain.exception.CategoryNotFoundException;
import com.personalfinance.transactionservice.domain.model.Category;
import com.personalfinance.transactionservice.domain.model.TransactionType;
import com.personalfinance.transactionservice.domain.port.CategoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class CategoryApplicationService implements CategoryUseCase {

    private final CategoryRepository categoryRepository;

    public CategoryApplicationService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Override
    @Transactional
    public CategoryDto create(UUID userId, CreateCategoryCommand command) {
        if (command.parentId() != null) {
            Category parent = categoryRepository.findById(command.parentId())
                    .orElseThrow(() -> new CategoryNotFoundException("Parent category not found"));
            if (!parent.isAccessibleBy(userId)) {
                throw new CategoryNotFoundException("Parent category not found");
            }
            if (!parent.isTopLevel()) {
                throw new IllegalArgumentException("Subcategories cannot be nested more than one level");
            }
            if (parent.getTransactionType() != command.transactionType()) {
                throw new IllegalArgumentException("Subcategory must have the same transaction type as its parent");
            }
        }

        if (categoryRepository.existsByNameAndParentIdAndUserIdAndTransactionType(
                command.name(), command.parentId(), userId, command.transactionType())) {
            throw new IllegalArgumentException("Category with this name already exists");
        }

        Category category = Category.createCustom(userId, command.name(), command.parentId(), command.transactionType());
        return toDto(categoryRepository.save(category));
    }

    @Override
    public CategoryDto getById(UUID userId, UUID categoryId) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new CategoryNotFoundException("Category not found"));
        if (!category.isAccessibleBy(userId)) {
            throw new CategoryNotFoundException("Category not found");
        }
        return toDto(category);
    }

    @Override
    public List<CategoryDto> listAccessible(UUID userId, TransactionType transactionType) {
        List<Category> categories = transactionType != null
                ? categoryRepository.findAllAccessibleByUserAndType(userId, transactionType)
                : categoryRepository.findAllAccessibleByUser(userId);
        return categories.stream().map(this::toDto).toList();
    }

    @Override
    public List<CategoryDto> listSubcategories(UUID userId, UUID parentId) {
        Category parent = categoryRepository.findById(parentId)
                .orElseThrow(() -> new CategoryNotFoundException("Category not found"));
        if (!parent.isAccessibleBy(userId)) {
            throw new CategoryNotFoundException("Category not found");
        }
        return categoryRepository.findByParentId(parentId).stream()
                .filter(c -> c.isAccessibleBy(userId))
                .map(this::toDto).toList();
    }

    @Override
    @Transactional
    public void delete(UUID userId, UUID categoryId) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new CategoryNotFoundException("Category not found"));
        if (category.isDefault()) {
            throw new IllegalArgumentException("Cannot delete a default category");
        }
        if (!category.isAccessibleBy(userId)) {
            throw new CategoryNotFoundException("Category not found");
        }
        List<Category> children = categoryRepository.findByParentId(categoryId);
        for (Category child : children) {
            categoryRepository.deleteById(child.getId());
        }
        categoryRepository.deleteById(categoryId);
    }

    private CategoryDto toDto(Category c) {
        return new CategoryDto(c.getId(), c.getUserId(), c.getName(),
                c.getParentId(), c.getTransactionType(), c.isDefault(), c.getCreatedAt());
    }
}

