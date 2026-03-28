package com.personalfinance.transactionservice.application.service;

import com.personalfinance.transactionservice.application.dto.CategoryDto;
import com.personalfinance.transactionservice.application.dto.CreateCategoryCommand;
import com.personalfinance.transactionservice.domain.exception.CategoryNotFoundException;
import com.personalfinance.transactionservice.domain.model.Category;
import com.personalfinance.transactionservice.domain.model.TransactionType;
import com.personalfinance.transactionservice.domain.port.CategoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryApplicationServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    private CategoryApplicationService service;

    private final UUID userId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        service = new CategoryApplicationService(categoryRepository);
    }

    @Test
    void createTopLevelCategory() {
        CreateCategoryCommand command = new CreateCategoryCommand("Custom Food", null, TransactionType.EXPENSE);
        when(categoryRepository.existsByNameAndParentIdAndUserIdAndTransactionType("Custom Food", null, userId, TransactionType.EXPENSE))
                .thenReturn(false);
        when(categoryRepository.save(any(Category.class))).thenAnswer(inv -> inv.getArgument(0));

        CategoryDto result = service.create(userId, command);

        assertThat(result.name()).isEqualTo("Custom Food");
        assertThat(result.parentId()).isNull();
        assertThat(result.transactionType()).isEqualTo(TransactionType.EXPENSE);
        assertThat(result.isDefault()).isFalse();
        verify(categoryRepository).save(any(Category.class));
    }

    @Test
    void createSubcategory() {
        UUID parentId = UUID.randomUUID();
        Category parent = new Category(parentId, null, "Food", null, TransactionType.EXPENSE, OffsetDateTime.now());
        CreateCategoryCommand command = new CreateCategoryCommand("Sushi", parentId, TransactionType.EXPENSE);

        when(categoryRepository.findById(parentId)).thenReturn(Optional.of(parent));
        when(categoryRepository.existsByNameAndParentIdAndUserIdAndTransactionType("Sushi", parentId, userId, TransactionType.EXPENSE))
                .thenReturn(false);
        when(categoryRepository.save(any(Category.class))).thenAnswer(inv -> inv.getArgument(0));

        CategoryDto result = service.create(userId, command);

        assertThat(result.name()).isEqualTo("Sushi");
        assertThat(result.parentId()).isEqualTo(parentId);
    }

    @Test
    void createSubcategoryWithNonExistentParentThrows() {
        UUID parentId = UUID.randomUUID();
        CreateCategoryCommand command = new CreateCategoryCommand("Sub", parentId, TransactionType.EXPENSE);
        when(categoryRepository.findById(parentId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.create(userId, command))
                .isInstanceOf(CategoryNotFoundException.class);
    }

    @Test
    void createSubcategoryUnderSubcategoryThrows() {
        UUID grandParentId = UUID.randomUUID();
        UUID parentId = UUID.randomUUID();
        Category parent = new Category(parentId, null, "Sub", grandParentId, TransactionType.EXPENSE, OffsetDateTime.now());
        CreateCategoryCommand command = new CreateCategoryCommand("SubSub", parentId, TransactionType.EXPENSE);

        when(categoryRepository.findById(parentId)).thenReturn(Optional.of(parent));

        assertThatThrownBy(() -> service.create(userId, command))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("nested");
    }

    @Test
    void createSubcategoryWithMismatchedTypeThrows() {
        UUID parentId = UUID.randomUUID();
        Category parent = new Category(parentId, null, "Food", null, TransactionType.EXPENSE, OffsetDateTime.now());
        CreateCategoryCommand command = new CreateCategoryCommand("Salary Sub", parentId, TransactionType.INCOME);

        when(categoryRepository.findById(parentId)).thenReturn(Optional.of(parent));

        assertThatThrownBy(() -> service.create(userId, command))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("same transaction type");
    }

    @Test
    void createDuplicateCategoryThrows() {
        CreateCategoryCommand command = new CreateCategoryCommand("Food", null, TransactionType.EXPENSE);
        when(categoryRepository.existsByNameAndParentIdAndUserIdAndTransactionType("Food", null, userId, TransactionType.EXPENSE))
                .thenReturn(true);

        assertThatThrownBy(() -> service.create(userId, command))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("already exists");
    }

    @Test
    void getByIdReturnsCategory() {
        UUID categoryId = UUID.randomUUID();
        Category category = new Category(categoryId, null, "Food", null, TransactionType.EXPENSE, OffsetDateTime.now());
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category));

        CategoryDto result = service.getById(userId, categoryId);

        assertThat(result.id()).isEqualTo(categoryId);
        assertThat(result.name()).isEqualTo("Food");
    }

    @Test
    void getByIdNotFoundThrows() {
        UUID categoryId = UUID.randomUUID();
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getById(userId, categoryId))
                .isInstanceOf(CategoryNotFoundException.class);
    }

    @Test
    void getByIdInaccessibleThrows() {
        UUID categoryId = UUID.randomUUID();
        UUID otherUserId = UUID.randomUUID();
        Category category = new Category(categoryId, otherUserId, "Private", null, TransactionType.EXPENSE, OffsetDateTime.now());
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category));

        assertThatThrownBy(() -> service.getById(userId, categoryId))
                .isInstanceOf(CategoryNotFoundException.class);
    }

    @Test
    void listAccessibleWithoutTypeFilter() {
        Category c1 = new Category(UUID.randomUUID(), null, "Food", null, TransactionType.EXPENSE, OffsetDateTime.now());
        Category c2 = new Category(UUID.randomUUID(), userId, "Custom", null, TransactionType.INCOME, OffsetDateTime.now());
        when(categoryRepository.findAllAccessibleByUser(userId)).thenReturn(List.of(c1, c2));

        List<CategoryDto> result = service.listAccessible(userId, null);

        assertThat(result).hasSize(2);
    }

    @Test
    void listAccessibleWithTypeFilter() {
        Category c1 = new Category(UUID.randomUUID(), null, "Food", null, TransactionType.EXPENSE, OffsetDateTime.now());
        when(categoryRepository.findAllAccessibleByUserAndType(userId, TransactionType.EXPENSE)).thenReturn(List.of(c1));

        List<CategoryDto> result = service.listAccessible(userId, TransactionType.EXPENSE);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).transactionType()).isEqualTo(TransactionType.EXPENSE);
    }

    @Test
    void listSubcategories() {
        UUID parentId = UUID.randomUUID();
        Category parent = new Category(parentId, null, "Food", null, TransactionType.EXPENSE, OffsetDateTime.now());
        Category child = new Category(UUID.randomUUID(), null, "Groceries", parentId, TransactionType.EXPENSE, OffsetDateTime.now());
        when(categoryRepository.findById(parentId)).thenReturn(Optional.of(parent));
        when(categoryRepository.findByParentId(parentId)).thenReturn(List.of(child));

        List<CategoryDto> result = service.listSubcategories(userId, parentId);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).name()).isEqualTo("Groceries");
    }

    @Test
    void deleteCustomCategory() {
        UUID categoryId = UUID.randomUUID();
        Category category = new Category(categoryId, userId, "Custom", null, TransactionType.EXPENSE, OffsetDateTime.now());
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category));
        when(categoryRepository.findByParentId(categoryId)).thenReturn(List.of());

        service.delete(userId, categoryId);

        verify(categoryRepository).deleteById(categoryId);
    }

    @Test
    void deleteCustomCategoryAlsoDeletesChildren() {
        UUID categoryId = UUID.randomUUID();
        UUID childId = UUID.randomUUID();
        Category category = new Category(categoryId, userId, "Custom", null, TransactionType.EXPENSE, OffsetDateTime.now());
        Category child = new Category(childId, userId, "SubCustom", categoryId, TransactionType.EXPENSE, OffsetDateTime.now());
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category));
        when(categoryRepository.findByParentId(categoryId)).thenReturn(List.of(child));

        service.delete(userId, categoryId);

        verify(categoryRepository).deleteById(childId);
        verify(categoryRepository).deleteById(categoryId);
    }

    @Test
    void deleteDefaultCategoryThrows() {
        UUID categoryId = UUID.randomUUID();
        Category category = new Category(categoryId, null, "Food", null, TransactionType.EXPENSE, OffsetDateTime.now());
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category));

        assertThatThrownBy(() -> service.delete(userId, categoryId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("default");
    }

    @Test
    void deleteOtherUsersCategoryThrows() {
        UUID categoryId = UUID.randomUUID();
        UUID otherUserId = UUID.randomUUID();
        Category category = new Category(categoryId, otherUserId, "Other", null, TransactionType.EXPENSE, OffsetDateTime.now());
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category));

        assertThatThrownBy(() -> service.delete(userId, categoryId))
                .isInstanceOf(CategoryNotFoundException.class);
    }
}

