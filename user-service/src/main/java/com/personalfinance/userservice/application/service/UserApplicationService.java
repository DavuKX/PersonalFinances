package com.personalfinance.userservice.application.service;

import com.personalfinance.userservice.application.dto.RegisterCommand;
import com.personalfinance.userservice.application.dto.UserDto;
import com.personalfinance.userservice.application.usecase.UserUseCase;
import com.personalfinance.userservice.domain.exception.UserAlreadyExistsException;
import com.personalfinance.userservice.domain.model.User;
import com.personalfinance.userservice.domain.port.PasswordHasher;
import com.personalfinance.userservice.domain.port.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class UserApplicationService implements UserUseCase {

    private final UserRepository userRepository;
    private final PasswordHasher passwordHasher;

    public UserApplicationService(UserRepository userRepository, PasswordHasher passwordHasher) {
        this.userRepository = userRepository;
        this.passwordHasher = passwordHasher;
    }

    @Override
    @Transactional
    public UserDto register(RegisterCommand command) {
        if (userRepository.existsByEmail(command.email())) {
            throw new UserAlreadyExistsException("Email already in use");
        }
        if (userRepository.existsByUsername(command.username())) {
            throw new UserAlreadyExistsException("Username already in use");
        }

        User user = User.register(command.username(), command.email(), passwordHasher.hash(command.password()));
        User saved = userRepository.save(user);
        return toDto(saved);
    }

    @Override
    public UserDto findById(UUID id) {
        return userRepository.findById(id)
                .map(this::toDto)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
    }

    public UserDto toDto(User user) {
        return new UserDto(user.getId(), user.getUsername(), user.getEmail(), user.getRoles(), user.getCreatedAt());
    }
}
