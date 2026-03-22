package com.personalfinance.userservice.application.service;

import com.personalfinance.userservice.application.dto.ChangePasswordCommand;
import com.personalfinance.userservice.application.dto.RegisterCommand;
import com.personalfinance.userservice.application.dto.UpdateProfileCommand;
import com.personalfinance.userservice.application.dto.UserDto;
import com.personalfinance.userservice.domain.exception.AccessDeniedException;
import com.personalfinance.userservice.domain.exception.InvalidPasswordException;
import com.personalfinance.userservice.domain.exception.UserAlreadyExistsException;
import com.personalfinance.userservice.domain.exception.UserNotFoundException;
import com.personalfinance.userservice.domain.model.User;
import com.personalfinance.userservice.domain.port.PasswordHasher;
import com.personalfinance.userservice.domain.port.UserRepository;
import org.junit.jupiter.api.BeforeEach;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserApplicationServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private PasswordHasher passwordHasher;

    @InjectMocks
    private UserApplicationService userService;

    private User alice;
    private UUID aliceId;

    @BeforeEach
    void setUp() {
        aliceId = UUID.randomUUID();
        alice = new User(aliceId, "alice", "alice@example.com", "hashed", Set.of("ROLE_USER"), OffsetDateTime.now());
    }

    @Test
    void register_returnsUserDto_whenCommandIsValid() {
        when(userRepository.existsByEmail("alice@example.com")).thenReturn(false);
        when(userRepository.existsByUsername("alice")).thenReturn(false);
        when(passwordHasher.hash("StrongPass123!")).thenReturn("hashed");
        when(userRepository.save(any())).thenReturn(alice);

        UserDto dto = userService.register(new RegisterCommand("alice", "alice@example.com", "StrongPass123!"));

        assertEquals("alice", dto.username());
        assertEquals("alice@example.com", dto.email());
        assertTrue(dto.roles().contains("ROLE_USER"));
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
        when(userRepository.findById(aliceId)).thenReturn(Optional.of(alice));

        UserDto dto = userService.findById(aliceId);

        assertEquals(aliceId, dto.id());
        assertEquals("alice", dto.username());
    }

    @Test
    void findById_throws_whenUserNotFound() {
        UUID id = UUID.randomUUID();
        when(userRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> userService.findById(id));
    }

    @Test
    void updateProfile_updatesAndReturnsUser_whenRequesterIsOwner() {
        when(userRepository.findById(aliceId)).thenReturn(Optional.of(alice));
        when(userRepository.findByEmail("new@example.com")).thenReturn(Optional.empty());
        when(userRepository.findByUsername("alicenew")).thenReturn(Optional.empty());
        User updated = new User(aliceId, "alicenew", "new@example.com", "hashed", Set.of("ROLE_USER"), alice.getCreatedAt());
        when(userRepository.save(any())).thenReturn(updated);

        UserDto dto = userService.updateProfile(aliceId, aliceId, new UpdateProfileCommand("alicenew", "new@example.com"));

        assertEquals("alicenew", dto.username());
        assertEquals("new@example.com", dto.email());
    }

    @Test
    void updateProfile_throws_whenRequesterIsNotOwner() {
        UUID otherId = UUID.randomUUID();

        assertThrows(AccessDeniedException.class,
                () -> userService.updateProfile(otherId, aliceId, new UpdateProfileCommand("alicenew", "new@example.com")));
    }

    @Test
    void updateProfile_throws_whenEmailTakenByAnotherUser() {
        UUID otherId = UUID.randomUUID();
        User other = new User(otherId, "bob", "taken@example.com", "hashed", Set.of("ROLE_USER"), OffsetDateTime.now());
        when(userRepository.findById(aliceId)).thenReturn(Optional.of(alice));
        when(userRepository.findByEmail("taken@example.com")).thenReturn(Optional.of(other));

        assertThrows(UserAlreadyExistsException.class,
                () -> userService.updateProfile(aliceId, aliceId, new UpdateProfileCommand("alice", "taken@example.com")));
    }

    @Test
    void changePassword_succeeds_whenCurrentPasswordIsCorrect() {
        when(userRepository.findById(aliceId)).thenReturn(Optional.of(alice));
        when(passwordHasher.matches("OldPass123!", "hashed")).thenReturn(true);
        when(passwordHasher.hash("NewPass123!")).thenReturn("newHashed");
        when(userRepository.save(any())).thenReturn(alice);

        assertDoesNotThrow(() -> userService.changePassword(aliceId, aliceId, new ChangePasswordCommand("OldPass123!", "NewPass123!")));

        verify(userRepository).save(any());
    }

    @Test
    void changePassword_throws_whenCurrentPasswordIsWrong() {
        when(userRepository.findById(aliceId)).thenReturn(Optional.of(alice));
        when(passwordHasher.matches("WrongPass!", "hashed")).thenReturn(false);

        assertThrows(InvalidPasswordException.class,
                () -> userService.changePassword(aliceId, aliceId, new ChangePasswordCommand("WrongPass!", "NewPass123!")));
    }

    @Test
    void changePassword_throws_whenRequesterIsNotOwner() {
        UUID otherId = UUID.randomUUID();

        assertThrows(AccessDeniedException.class,
                () -> userService.changePassword(otherId, aliceId, new ChangePasswordCommand("OldPass123!", "NewPass123!")));
    }
}
