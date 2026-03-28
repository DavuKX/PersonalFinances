package com.personalfinance.transactionservice.infrastructure.persistence.entity;

import com.personalfinance.transactionservice.domain.model.TransactionType;
import jakarta.persistence.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "categories", indexes = {
        @Index(name = "idx_cat_user_id", columnList = "userId"),
        @Index(name = "idx_cat_parent_id", columnList = "parentId"),
        @Index(name = "idx_cat_transaction_type", columnList = "transactionType")
}, uniqueConstraints = {
        @UniqueConstraint(name = "uq_cat_name_parent_user_type",
                columnNames = {"name", "parentId", "userId", "transactionType"})
})
public class CategoryJpaEntity {

    @Id
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Column(columnDefinition = "uuid")
    private UUID userId;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(columnDefinition = "uuid")
    private UUID parentId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private TransactionType transactionType;

    @Column(nullable = false)
    private OffsetDateTime createdAt;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public UUID getParentId() { return parentId; }
    public void setParentId(UUID parentId) { this.parentId = parentId; }

    public TransactionType getTransactionType() { return transactionType; }
    public void setTransactionType(TransactionType transactionType) { this.transactionType = transactionType; }

    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }
}

