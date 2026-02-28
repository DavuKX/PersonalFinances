package com.personalfinance.userservice.service;

import com.personalfinance.userservice.dto.RegisterRequest;
import com.personalfinance.userservice.dto.UserResponse;
import com.personalfinance.userservice.entity.User;
import com.personalfinance.userservice.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    @Test
    void register_shouldReturnUserResponse() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("alice");
        request.setEmail("alice@example.com");
        request.setPassword("StrongPass123!");

        when(userRepository.existsByEmail("alice@example.com")).thenReturn(false);
        when(userRepository.existsByUsername("alice")).thenReturn(false);
        when(passwordEncoder.encode("StrongPass123!")).thenReturn("encoded");

        User saved = new User();
        saved.setId(UUID.randomUUID());
        saved.setUsername("alice");
        saved.setEmail("alice@example.com");
        saved.setPassword("encoded");
        saved.setRoles(Set.of("ROLE_USER"));
        when(userRepository.save(any(User.class))).thenReturn(saved);

        UserResponse response = userService.register(request);
        assertEquals("alice", response.getUsername());
        assertEquals("alice@example.com", response.getEmail());
        assertNotNull(response.getId());
        assertTrue(response.getRoles().contains("ROLE_USER"));
    }

    @Test
    void register_shouldThrow_whenEmailExists() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("alice");
        request.setEmail("alice@example.com");
        request.setPassword("StrongPass123!");

        when(userRepository.existsByEmail("alice@example.com")).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> userService.register(request));
    }

    @Test
    void register_shouldThrow_whenUsernameExists() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("alice");
        request.setEmail("alice@example.com");
        request.setPassword("StrongPass123!");

        when(userRepository.existsByEmail("alice@example.com")).thenReturn(false);
        when(userRepository.existsByUsername("alice")).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> userService.register(request));
    }
}
