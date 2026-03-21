package com.psygst.security;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

/**
 * Custom UserDetails holding all claims needed for multi-tenant isolation.
 * RN-S01: idSistema is the critical tenant filter — always taken from JWT,
 * never from request body.
 */
@Getter
@AllArgsConstructor
public class PsygstUserDetails implements UserDetails {

    private final Integer idAuth;
    private final Integer idProfesional;
    private final Integer idSistema;
    private final Integer idRol;
    private final String username;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        String role = (idRol == 1) ? "ROLE_ADMIN" : "ROLE_PROFESIONAL";
        return List.of(new SimpleGrantedAuthority(role));
    }

    @Override
    public String getPassword() {
        return null;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
