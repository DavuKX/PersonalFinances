package com.personalfinance.userservice.infrastructure.persistence.adapter;

import com.personalfinance.userservice.domain.model.RefreshToken;
import com.personalfinance.userservice.domain.port.RefreshTokenRepository;
import com.personalfinance.userservice.infrastructure.persistence.mapper.RefreshTokenJpaMapper;
import com.personalfinance.userservice.infrastructure.persistence.repository.RefreshTokenJpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public class RefreshTokenRepositoryAdapter implements RefreshTokenRepository {

    private final RefreshTokenJpaRepository jpaRepository;

    public RefreshTokenRepositoryAdapter(RefreshTokenJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public RefreshToken save(RefreshToken refreshToken) {
        return RefreshTokenJpaMapper.toDomain(jpaRepository.save(RefreshTokenJpaMapper.toEntity(refreshToken)));
    }

    @Override
    public Optional<RefreshToken> findByToken(String token) {
        return jpaRepository.findByToken(token).map(RefreshTokenJpaMapper::toDomain);
    }

    @Override
    public void revokeAllByUserId(UUID userId) {
        jpaRepository.revokeAllByUserId(userId);
    }
}
