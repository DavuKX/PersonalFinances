package com.personalfinance.userservice.application.service;

import com.personalfinance.userservice.application.dto.UpdateRolesCommand;
import com.personalfinance.userservice.application.dto.UserDto;
import com.personalfinance.userservice.application.dto.UserPageDto;
import com.personalfinance.userservice.application.usecase.AdminUseCase;
import com.personalfinance.userservice.domain.exception.UserNotFoundException;
import com.personalfinance.userservice.domain.model.User;
import com.personalfinance.userservice.domain.port.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class AdminApplicationService implements AdminUseCase {

    private final UserRepository userRepository;

    public AdminApplicationService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserPageDto listUsers(Pageable pageable) {
        Page<User> page = userRepository.findAll(pageable);
        return new UserPageDto(
                page.getContent().stream().map(this::toDto).toList(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages()
        );
    }

    @Override
    public UserDto getUser(UUID id) {
        return toDto(loadUser(id));
    }

    @Override
    @Transactional
    public UserDto updateRoles(UUID id, UpdateRolesCommand command) {
        User user = loadUser(id);
        return toDto(userRepository.save(user.withRoles(command.roles())));
    }

    @Override
    @Transactional
    public void deleteUser(UUID id) {
        loadUser(id);
        userRepository.deleteById(id);
    }

    private User loadUser(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
    }

    private UserDto toDto(User user) {
        return new UserDto(user.getId(), user.getUsername(), user.getEmail(), user.getRoles(), user.getCreatedAt());
    }
}
