package com.personalfinance.userservice.infrastructure.persistence.mapper;

import com.personalfinance.userservice.domain.model.User;
import com.personalfinance.userservice.infrastructure.persistence.entity.UserJpaEntity;

public class UserJpaMapper {

    private UserJpaMapper() {}

    public static UserJpaEntity toEntity(User user) {
        UserJpaEntity entity = new UserJpaEntity();
        entity.setId(user.getId());
        entity.setUsername(user.getUsername());
        entity.setEmail(user.getEmail());
        entity.setHashedPassword(user.getHashedPassword());
        entity.setRoles(user.getRoles());
        entity.setCreatedAt(user.getCreatedAt());
        return entity;
    }

    public static User toDomain(UserJpaEntity entity) {
        return new User(
                entity.getId(),
                entity.getUsername(),
                entity.getEmail(),
                entity.getHashedPassword(),
                entity.getRoles(),
                entity.getCreatedAt()
        );
    }
}
