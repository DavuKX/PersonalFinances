package com.personalfinance.userservice.application.dto;

import java.util.Set;

public record UpdateRolesCommand(Set<String> roles) {}
