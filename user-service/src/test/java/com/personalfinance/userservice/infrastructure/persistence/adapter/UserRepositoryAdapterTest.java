package com.personalfinance.userservice.infrastructure.persistence.adapter;

import com.personalfinance.userservice.domain.model.User;
import com.personalfinance.userservice.infrastructure.persistence.entity.UserJpaEntity;
import com.personalfinance.userservice.infrastructure.persistence.repository.UserJpaRepository;
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
class UserRepositoryAdapterTest {

    @Mock private UserJpaRepository jpaRepository;

    @InjectMocks
    private UserRepositoryAdapter adapter;

    private UserJpaEntity sampleEntity(UUID id) {
        UserJpaEntity e = new UserJpaEntity();
        e.setId(id);
        e.setUsername("alice");
        e.setEmail("alice@example.com");
        e.setHashedPassword("hashed");
        e.setRoles(Set.of("ROLE_USER"));
        e.setCreatedAt(OffsetDateTime.now());
        return e;
    }

    @Test
    void findById_returnsDomainUser_whenEntityExists() {
        UUID id = UUID.randomUUID();
        when(jpaRepository.findById(id)).thenReturn(Optional.of(sampleEntity(id)));

        Optional<User> result = adapter.findById(id);

        assertTrue(result.isPresent());
        assertEquals("alice", result.get().getUsername());
        assertEquals(id, result.get().getId());
    }

    @Test
    void findByEmail_returnsEmpty_whenNotFound() {
        when(jpaRepository.findByEmail("unknown@example.com")).thenReturn(Optional.empty());

        assertTrue(adapter.findByEmail("unknown@example.com").isEmpty());
    }

    @Test
    void save_returnsMappedDomainUser() {
        UUID id = UUID.randomUUID();
        User user = new User(id, "alice", "alice@example.com", "hashed", Set.of("ROLE_USER"), OffsetDateTime.now());
        when(jpaRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        User saved = adapter.save(user);

        assertEquals(id, saved.getId());
        assertEquals("alice", saved.getUsername());
    }

    @Test
    void existsByEmail_delegatesToJpaRepository() {
        when(jpaRepository.existsByEmail("alice@example.com")).thenReturn(true);

        assertTrue(adapter.existsByEmail("alice@example.com"));
    }

    @Test
    void existsByUsername_delegatesToJpaRepository() {
        when(jpaRepository.existsByUsername("alice")).thenReturn(false);

        assertFalse(adapter.existsByUsername("alice"));
    }
}
