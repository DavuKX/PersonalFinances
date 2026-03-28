package com.personalfinance.transactionservice.infrastructure.persistence.adapter;

import com.personalfinance.transactionservice.domain.model.Category;
import com.personalfinance.transactionservice.domain.model.TransactionType;
import com.personalfinance.transactionservice.domain.port.CategoryRepository;
import com.personalfinance.transactionservice.infrastructure.persistence.mapper.CategoryJpaMapper;
import com.personalfinance.transactionservice.infrastructure.persistence.repository.CategoryJpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class CategoryRepositoryAdapter implements CategoryRepository {

    private final CategoryJpaRepository jpaRepository;

    public CategoryRepositoryAdapter(CategoryJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Category save(Category category) {
        return CategoryJpaMapper.toDomain(jpaRepository.save(CategoryJpaMapper.toEntity(category)));
    }

    @Override
    public Optional<Category> findById(UUID id) {
        return jpaRepository.findById(id).map(CategoryJpaMapper::toDomain);
    }

    @Override
    public List<Category> findAllAccessibleByUser(UUID userId) {
        return jpaRepository.findAllAccessibleByUser(userId).stream()
                .map(CategoryJpaMapper::toDomain).toList();
    }

    @Override
    public List<Category> findAllAccessibleByUserAndType(UUID userId, TransactionType transactionType) {
        return jpaRepository.findAllAccessibleByUserAndType(userId, transactionType).stream()
                .map(CategoryJpaMapper::toDomain).toList();
    }

    @Override
    public List<Category> findByParentId(UUID parentId) {
        return jpaRepository.findByParentId(parentId).stream()
                .map(CategoryJpaMapper::toDomain).toList();
    }

    @Override
    public boolean existsByNameAndParentIdAndUserIdAndTransactionType(String name, UUID parentId, UUID userId, TransactionType transactionType) {
        return jpaRepository.existsByNameAndParentIdAndUserIdAndTransactionType(name, parentId, userId, transactionType);
    }

    @Override
    public void deleteById(UUID id) {
        jpaRepository.deleteById(id);
    }

    @Override
    public long countByUserIdIsNull() {
        return jpaRepository.countByUserIdIsNull();
    }
}

