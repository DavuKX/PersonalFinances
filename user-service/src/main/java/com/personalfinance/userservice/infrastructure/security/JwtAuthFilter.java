package com.personalfinance.userservice.infrastructure.security;

import com.personalfinance.userservice.domain.model.User;
import com.personalfinance.userservice.domain.port.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserRepository userRepository;

    public JwtAuthFilter(JwtService jwtService, UserRepository userRepository) {
        this.jwtService = jwtService;
        this.userRepository = userRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String userId = request.getHeader("X-User-Id");
        if (userId != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            userRepository.findById(UUID.fromString(userId)).ifPresent(user -> {
                var authToken = buildAuthToken(user);
                SecurityContextHolder.getContext().setAuthentication(authToken);
            });
        }

        if (SecurityContextHolder.getContext().getAuthentication() == null) {
            String authHeader = request.getHeader("Authorization");
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
                if (jwtService.isTokenValid(token)) {
                    String email = jwtService.extractEmail(token);
                    if (email != null) {
                        userRepository.findByEmail(email).ifPresent(user -> {
                            SecurityContextHolder.getContext().setAuthentication(buildAuthToken(user));
                        });
                    }
                }
            }
        }

        filterChain.doFilter(request, response);
    }

    private UsernamePasswordAuthenticationToken buildAuthToken(User user) {
        var authorities = user.getRoles().stream()
                .map(SimpleGrantedAuthority::new)
                .toList();
        return new UsernamePasswordAuthenticationToken(user, null, authorities);
    }
}
