package com.personalfinance.transactionservice.infrastructure.config;

import com.personalfinance.transactionservice.domain.model.Category;
import com.personalfinance.transactionservice.domain.port.CategoryRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DefaultCategorySeederTest {

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private DefaultCategorySeeder seeder;

    @Test
    void seedsDefaultCategoriesWhenNoneExist() {
        when(categoryRepository.countByUserIdIsNull()).thenReturn(0L);
        when(categoryRepository.save(any(Category.class))).thenAnswer(inv -> inv.getArgument(0));

        seeder.run(null);

        verify(categoryRepository, atLeastOnce()).save(any(Category.class));
    }

    @Test
    void skipsSeeedingWhenDefaultCategoriesExist() {
        when(categoryRepository.countByUserIdIsNull()).thenReturn(50L);

        seeder.run(null);

        verify(categoryRepository, never()).save(any(Category.class));
    }
}

