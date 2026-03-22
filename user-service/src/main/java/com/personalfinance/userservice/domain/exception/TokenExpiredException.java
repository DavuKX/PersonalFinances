package com.personalfinance.userservice.domain.exception;

public class TokenExpiredException extends RuntimeException {
    public TokenExpiredException() {
        super("Refresh token has expired");
    }
}
