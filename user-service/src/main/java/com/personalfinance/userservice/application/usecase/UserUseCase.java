package com.personalfinance.userservice.application.usecase;

import com.personalfinance.userservice.application.dto.RegisterCommand;
import com.personalfinance.userservice.application.dto.UserDto;

import java.util.UUID;

public interface UserUseCase {
    UserDto register(RegisterCommand command);
    UserDto findById(UUID id);
}
