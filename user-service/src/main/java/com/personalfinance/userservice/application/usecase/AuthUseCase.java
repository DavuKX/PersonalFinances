package com.personalfinance.userservice.application.usecase;

import com.personalfinance.userservice.application.dto.LoginCommand;
import com.personalfinance.userservice.application.dto.TokenPair;

import java.util.UUID;

public interface AuthUseCase {
    TokenPair login(LoginCommand command);
    TokenPair refresh(String refreshToken);
    void logout(UUID userId, String jti);
}
