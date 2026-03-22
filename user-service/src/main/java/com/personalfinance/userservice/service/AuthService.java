package com.personalfinance.userservice.service;

import com.personalfinance.userservice.dto.LoginRequest;
import com.personalfinance.userservice.dto.LoginResponse;
import com.personalfinance.userservice.entity.RefreshToken;
import com.personalfinance.userservice.entity.User;
import com.personalfinance.userservice.repository.UserRepository;
import com.personalfinance.userservice.security.JwtService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Map;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final TokenBlocklistService tokenBlocklistService;
    private final long accessExpirationMs;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService,
                       RefreshTokenService refreshTokenService,
                       TokenBlocklistService tokenBlocklistService,
                       @Value("${jwt.expiration-ms}") long accessExpirationMs) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.refreshTokenService = refreshTokenService;
        this.tokenBlocklistService = tokenBlocklistService;
        this.accessExpirationMs = accessExpirationMs;
    }

    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("Invalid email or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Invalid email or password");
        }

        return buildLoginResponse(user);
    }

    public LoginResponse refresh(String rawRefreshToken) {
        RefreshToken refreshToken = refreshTokenService.validateRefreshToken(rawRefreshToken);
        refreshTokenService.revokeRefreshToken(rawRefreshToken);
        return buildLoginResponse(refreshToken.getUser());
    }

    public void logout(User user, String jti) {
        // jti was extracted and forwarded by the gateway — block it for the access token lifetime
        Date expiresAt = new Date(System.currentTimeMillis() + accessExpirationMs);
        tokenBlocklistService.block(jti, expiresAt);
        refreshTokenService.revokeAllTokensForUser(user);
    }

    private LoginResponse buildLoginResponse(User user) {
        String accessToken = jwtService.generateToken(user.getEmail(), Map.of(
                "userId", user.getId().toString(),
                "roles", user.getRoles()
        ), accessExpirationMs);

        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user);

        return new LoginResponse(accessToken, refreshToken.getToken(), accessExpirationMs / 1000);
    }
}
