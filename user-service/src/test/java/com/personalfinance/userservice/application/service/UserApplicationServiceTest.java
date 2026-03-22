package com.personalfinance.userservice.application.service;

import com.personalfinance.userservice.application.dto.RegisterCommand;
import com.personalfinance.userservice.application.dto.UserDto;
import com.personalfinance.userservice.domain.exception.UserAlreadyExistsException;
import com.personalfinance.userservice.domain.model.User;
import com.personalfinance.userservice.domain.port.PasswordHasher;
import com.personalfinance.userservice.domain.port.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserApplicationServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private PasswordHasher passwordHasher;

    @InjectMocks
    private UserApplicationService userService;

    @Test
    void register_returnsUserDto_whenCommandIsValid() {
        when(userRepository.existsByEmail("alice@example.com")).thenReturn(false);
        when(userRepository.existsByUsername("alice")).thenReturn(false);
        when(passwordHasher.hash("StrongPass123!")).thenReturn("hashed");

        User saved = new User(UUID.randomUUID(), "alice", "alice@example.com", "hashed", Set.of("ROLE_USER"), OffsetDateTime.now());
        when(userRepository.save(any())).thenReturn(saved);

        UserDto dto = userService.register(new RegisterCommand("alice", "alice@example.com", "StrongPass123!"));

        assertEquals("alice", dto.username());
        assertEquals("alice@example.com", dto.email());
        assertTrue(dto.roles().contains("ROLE_USER"));
        assertNotNull(dto.id());
    }

    @Test
    void register_throws_whenEmailAlreadyExists() {
        when(userRepository.existsByEmail("alice@example.com")).thenReturn(true);

        assertThrows(UserAlreadyExistsException.class,
                () -> userService.register(new RegisterCommand("alice", "alice@example.com", "StrongPass123!")));
    }

    @Test
    void register_throws_whenUsernameAlreadyExists() {
        when(userRepository.existsByEmail("alice@example.com")).thenReturn(false);
        when(userRepository.existsByUsername("alice")).thenReturn(true);

        assertThrows(UserAlreadyExistsException.class,
                () -> userService.register(new RegisterCommand("alice", "alice@example.com", "StrongPass123!")));
    }

    @Test
    void findById_returnsUserDto_whenUserExists() {
        UUID id = UUID.randomUUID();
        User user = new User(id, "alice", "alice@example.com", "hashed", Set.of("ROLE_USER"), OffsetDateTime.now());
        when(userRepository.findById(id)).thenReturn(Optional.of(user));

        UserDto dto = userService.findById(id);

        assertEquals(id, dto.id());
        assertEquals("alice", dto.username());
    }

    @Test
    void findById_throws_whenUserNotFound() {
        UUID id = UUID.randomUUID();
        when(userRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> userService.findById(id));
    }
}
