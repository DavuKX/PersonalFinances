package com.personalfinance.userservice.application.usecase;

import com.personalfinance.userservice.application.dto.UpdateRolesCommand;
import com.personalfinance.userservice.application.dto.UserDto;
import com.personalfinance.userservice.application.dto.UserPageDto;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface AdminUseCase {
    UserPageDto listUsers(Pageable pageable);
    UserDto getUser(UUID id);
    UserDto updateRoles(UUID id, UpdateRolesCommand command);
    void deleteUser(UUID id);
}
