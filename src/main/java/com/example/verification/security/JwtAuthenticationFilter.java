package com.example.verification.security;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            if (jwtTokenProvider.isValid(token)) {
                Claims claims = jwtTokenProvider.getClaims(token);
                Long userId = claims.get("userId", Long.class);
                String username = claims.getSubject();
                String roleCode = claims.get("roleCode", String.class);
                SecurityContextHolder.getContext().setAuthentication(new JwtAuthenticationToken(userId, username, roleCode == null ? "USER" : roleCode, token));
            }
        }
        filterChain.doFilter(request, response);
    }
}
