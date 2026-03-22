package com.personalfinance.userservice.application.dto;

import java.util.List;

public record UserPageDto(List<UserDto> content, int page, int size, long totalElements, int totalPages) {}
