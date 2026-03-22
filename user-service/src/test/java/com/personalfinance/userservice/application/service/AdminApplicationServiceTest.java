package com.personalfinance.userservice.application.service;

import com.personalfinance.userservice.application.dto.UpdateRolesCommand;
import com.personalfinance.userservice.application.dto.UserDto;
import com.personalfinance.userservice.application.dto.UserPageDto;
import com.personalfinance.userservice.domain.exception.UserNotFoundException;
import com.personalfinance.userservice.domain.model.User;
import com.personalfinance.userservice.domain.port.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminApplicationServiceTest {

    @Mock private UserRepository userRepository;

    @InjectMocks
    private AdminApplicationService adminService;

    private User alice;
    private UUID aliceId;

    @BeforeEach
    void setUp() {
        aliceId = UUID.randomUUID();
        alice = new User(aliceId, "alice", "alice@example.com", "hashed", Set.of("ROLE_USER"), OffsetDateTime.now());
    }

    @Test
    void listUsers_returnsPagedUsers() {
        PageRequest pageable = PageRequest.of(0, 10);
        when(userRepository.findAll(pageable)).thenReturn(new PageImpl<>(List.of(alice), pageable, 1));

        UserPageDto page = adminService.listUsers(pageable);

        assertEquals(1, page.totalElements());
        assertEquals(1, page.content().size());
        assertEquals("alice", page.content().get(0).username());
    }

    @Test
    void getUser_returnsUserDto_whenFound() {
        when(userRepository.findById(aliceId)).thenReturn(Optional.of(alice));

        UserDto dto = adminService.getUser(aliceId);

        assertEquals(aliceId, dto.id());
    }

    @Test
    void getUser_throws_whenNotFound() {
        UUID id = UUID.randomUUID();
        when(userRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> adminService.getUser(id));
    }

    @Test
    void updateRoles_savesAndReturnsUpdatedUser() {
        Set<String> newRoles = Set.of("ROLE_USER", "ROLE_ADMIN");
        User updated = new User(aliceId, "alice", "alice@example.com", "hashed", newRoles, alice.getCreatedAt());
        when(userRepository.findById(aliceId)).thenReturn(Optional.of(alice));
        when(userRepository.save(any())).thenReturn(updated);

        UserDto dto = adminService.updateRoles(aliceId, new UpdateRolesCommand(newRoles));

        assertTrue(dto.roles().contains("ROLE_ADMIN"));
        assertTrue(dto.roles().contains("ROLE_USER"));
    }

    @Test
    void deleteUser_deletesExistingUser() {
        when(userRepository.findById(aliceId)).thenReturn(Optional.of(alice));

        adminService.deleteUser(aliceId);

        verify(userRepository).deleteById(aliceId);
    }

    @Test
    void deleteUser_throws_whenUserNotFound() {
        UUID id = UUID.randomUUID();
        when(userRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> adminService.deleteUser(id));
    }
}
