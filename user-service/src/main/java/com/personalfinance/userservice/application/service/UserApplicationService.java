package com.personalfinance.userservice.application.service;

import com.personalfinance.userservice.application.dto.ChangePasswordCommand;
import com.personalfinance.userservice.application.dto.RegisterCommand;
import com.personalfinance.userservice.application.dto.UpdateProfileCommand;
import com.personalfinance.userservice.application.dto.UserDto;
import com.personalfinance.userservice.application.usecase.UserUseCase;
import com.personalfinance.userservice.domain.exception.AccessDeniedException;
import com.personalfinance.userservice.domain.exception.InvalidPasswordException;
import com.personalfinance.userservice.domain.exception.UserAlreadyExistsException;
import com.personalfinance.userservice.domain.exception.UserNotFoundException;
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
        return toDto(userRepository.save(user));
    }

    @Override
    public UserDto findById(UUID id) {
        return toDto(loadUser(id));
    }

    @Override
    @Transactional
    public UserDto updateProfile(UUID requesterId, UUID targetId, UpdateProfileCommand command) {
        if (!requesterId.equals(targetId)) {
            throw new AccessDeniedException();
        }
        User existing = loadUser(targetId);
        userRepository.findByEmail(command.email())
                .filter(u -> !u.getId().equals(targetId))
                .ifPresent(u -> { throw new UserAlreadyExistsException("Email already in use"); });
        userRepository.findByUsername(command.username())
                .filter(u -> !u.getId().equals(targetId))
                .ifPresent(u -> { throw new UserAlreadyExistsException("Username already in use"); });
        return toDto(userRepository.save(existing.withProfile(command.username(), command.email())));
    }

    @Override
    @Transactional
    public void changePassword(UUID requesterId, UUID targetId, ChangePasswordCommand command) {
        if (!requesterId.equals(targetId)) {
            throw new AccessDeniedException();
        }
        User user = loadUser(targetId);
        if (!passwordHasher.matches(command.currentPassword(), user.getHashedPassword())) {
            throw new InvalidPasswordException();
        }
        userRepository.save(user.withPassword(passwordHasher.hash(command.newPassword())));
    }

    public UserDto toDto(User user) {
        return new UserDto(user.getId(), user.getUsername(), user.getEmail(), user.getRoles(), user.getCreatedAt());
    }

    private User loadUser(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
    }
}
