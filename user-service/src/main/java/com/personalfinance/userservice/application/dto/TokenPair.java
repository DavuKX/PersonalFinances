package com.personalfinance.userservice.application.dto;

public record TokenPair(String accessToken, String refreshToken, long expiresInSeconds) {}
