package com.example.verification.security;

import lombok.Getter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;

@Getter
public class JwtAuthenticationToken extends AbstractAuthenticationToken {

    private final JwtUserPrincipal principal;
    private final String token;

    public JwtAuthenticationToken(Long userId, String username, String roleCode, String token) {
        super(List.of(new SimpleGrantedAuthority("ROLE_" + roleCode)));
        this.principal = new JwtUserPrincipal(userId, username, roleCode);
        this.token = token;
        setAuthenticated(true);
    }

    @Override
    public Object getCredentials() {
        return token;
    }

    @Override
    public Object getPrincipal() {
        return principal;
    }
}
