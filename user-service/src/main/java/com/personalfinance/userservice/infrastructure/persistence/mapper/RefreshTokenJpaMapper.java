package com.personalfinance.userservice.infrastructure.persistence.mapper;

import com.personalfinance.userservice.domain.model.RefreshToken;
import com.personalfinance.userservice.infrastructure.persistence.entity.RefreshTokenJpaEntity;

public class RefreshTokenJpaMapper {

    private RefreshTokenJpaMapper() {}

    public static RefreshTokenJpaEntity toEntity(RefreshToken token) {
        RefreshTokenJpaEntity entity = new RefreshTokenJpaEntity();
        entity.setId(token.getId());
        entity.setToken(token.getToken());
        entity.setUserId(token.getUserId());
        entity.setExpiresAt(token.getExpiresAt());
        entity.setRevoked(token.isRevoked());
        return entity;
    }

    public static RefreshToken toDomain(RefreshTokenJpaEntity entity) {
        return new RefreshToken(
                entity.getId(),
                entity.getToken(),
                entity.getUserId(),
                entity.getExpiresAt(),
                entity.isRevoked()
        );
    }
}
