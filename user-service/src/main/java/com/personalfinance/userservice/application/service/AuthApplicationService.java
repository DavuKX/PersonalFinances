package com.personalfinance.userservice.application.service;

import com.personalfinance.userservice.application.dto.LoginCommand;
import com.personalfinance.userservice.application.dto.TokenPair;
import com.personalfinance.userservice.application.usecase.AuthUseCase;
import com.personalfinance.userservice.domain.exception.InvalidCredentialsException;
import com.personalfinance.userservice.domain.exception.TokenExpiredException;
import com.personalfinance.userservice.domain.exception.TokenRevokedException;
import com.personalfinance.userservice.domain.model.RefreshToken;
import com.personalfinance.userservice.domain.model.User;
import com.personalfinance.userservice.domain.port.PasswordHasher;
import com.personalfinance.userservice.domain.port.RefreshTokenRepository;
import com.personalfinance.userservice.domain.port.TokenBlocklistPort;
import com.personalfinance.userservice.domain.port.TokenProvider;
import com.personalfinance.userservice.domain.port.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
public class AuthApplicationService implements AuthUseCase {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final TokenBlocklistPort tokenBlocklistPort;
    private final TokenProvider tokenProvider;
    private final PasswordHasher passwordHasher;
    private final long accessExpirationMs;
    private final long refreshExpirationMs;

    public AuthApplicationService(
            UserRepository userRepository,
            RefreshTokenRepository refreshTokenRepository,
            TokenBlocklistPort tokenBlocklistPort,
            TokenProvider tokenProvider,
            PasswordHasher passwordHasher,
            @Value("${jwt.expiration-ms}") long accessExpirationMs,
            @Value("${jwt.refresh-expiration-ms}") long refreshExpirationMs) {
        this.userRepository = userRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.tokenBlocklistPort = tokenBlocklistPort;
        this.tokenProvider = tokenProvider;
        this.passwordHasher = passwordHasher;
        this.accessExpirationMs = accessExpirationMs;
        this.refreshExpirationMs = refreshExpirationMs;
    }

    @Override
    @Transactional
    public TokenPair login(LoginCommand command) {
        User user = userRepository.findByEmail(command.email())
                .orElseThrow(InvalidCredentialsException::new);

        if (!passwordHasher.matches(command.password(), user.getHashedPassword())) {
            throw new InvalidCredentialsException();
        }

        return buildTokenPair(user);
    }

    @Override
    @Transactional
    public TokenPair refresh(String rawRefreshToken) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(rawRefreshToken)
                .orElseThrow(() -> new InvalidCredentialsException());

        if (refreshToken.isRevoked()) {
            throw new TokenRevokedException();
        }
        if (refreshToken.isExpired()) {
            throw new TokenExpiredException();
        }

        refreshToken.revoke();
        refreshTokenRepository.save(refreshToken);

        User user = userRepository.findById(refreshToken.getUserId())
                .orElseThrow(InvalidCredentialsException::new);

        return buildTokenPair(user);
    }

    @Override
    @Transactional
    public void logout(UUID userId, String jti) {
        Instant expiresAt = Instant.now().plusMillis(accessExpirationMs);
        tokenBlocklistPort.block(jti, expiresAt);
        refreshTokenRepository.revokeAllByUserId(userId);
    }

    private TokenPair buildTokenPair(User user) {
        refreshTokenRepository.revokeAllByUserId(user.getId());

        String accessToken = tokenProvider.generateAccessToken(user, accessExpirationMs);
        String refreshTokenValue = tokenProvider.generateRefreshTokenValue();

        RefreshToken refreshToken = new RefreshToken(
                UUID.randomUUID(),
                refreshTokenValue,
                user.getId(),
                Instant.now().plusMillis(refreshExpirationMs),
                false
        );
        refreshTokenRepository.save(refreshToken);

        return new TokenPair(accessToken, refreshTokenValue, accessExpirationMs / 1000);
    }
}
