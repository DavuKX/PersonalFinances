package com.personalfinance.userservice.application.dto;

public record ChangePasswordCommand(String currentPassword, String newPassword) {}
