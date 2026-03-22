package com.personalfinance.userservice.domain.port;

import com.personalfinance.userservice.domain.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository {
    Optional<User> findById(UUID id);
    Optional<User> findByEmail(String email);
    Optional<User> findByUsername(String username);
    boolean existsByEmail(String email);
    boolean existsByUsername(String username);
    User save(User user);
    Page<User> findAll(Pageable pageable);
    void deleteById(UUID id);
}
