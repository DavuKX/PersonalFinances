package com.personalfinance.userservice.application.dto;

import java.time.OffsetDateTime;
import java.util.Set;
import java.util.UUID;

public record UserDto(UUID id, String username, String email, Set<String> roles, OffsetDateTime createdAt) {}
