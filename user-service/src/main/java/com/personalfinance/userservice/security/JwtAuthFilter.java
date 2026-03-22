package com.personalfinance.userservice.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.personalfinance.userservice.entity.User;
import com.personalfinance.userservice.repository.UserRepository;

import java.io.IOException;

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

        // When coming through the API Gateway, the JWT is validated there and
        // the userId is forwarded as a trusted X-User-Id header.
        String userId = request.getHeader("X-User-Id");
        if (userId != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            User user = userRepository.findById(java.util.UUID.fromString(userId)).orElse(null);
            if (user != null) {
                var authorities = user.getRoles().stream()
                        .map(SimpleGrantedAuthority::new)
                        .toList();
                var authToken = new UsernamePasswordAuthenticationToken(user, null, authorities);
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }

        // Fallback: also support direct Bearer token (e.g. for tests bypassing the gateway)
        if (SecurityContextHolder.getContext().getAuthentication() == null) {
            String authHeader = request.getHeader("Authorization");
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
                if (jwtService.isTokenValid(token)) {
                    String email = jwtService.extractEmail(token);
                    if (email != null) {
                        User user = userRepository.findByEmail(email).orElse(null);
                        if (user != null) {
                            var authorities = user.getRoles().stream()
                                    .map(SimpleGrantedAuthority::new)
                                    .toList();
                            var authToken = new UsernamePasswordAuthenticationToken(user, null, authorities);
                            SecurityContextHolder.getContext().setAuthentication(authToken);
                        }
                    }
                }
            }
        }

        filterChain.doFilter(request, response);
    }
}
