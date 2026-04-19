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
 * 
 * Post UUID migration: all IDs are String (UUID format).
 */
@Getter
@AllArgsConstructor
public class PsygstUserDetails implements UserDetails {

    private final String idAuth;
    private final String idProfesional;
    private final String idSistema;
    private final String idRol;
    private final String username;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // idRol "1" = ADMIN by seed convention; role name stored in JWT as idRol string
        // We keep the numeric string comparison since idRol for lookup tables
        // was assigned a UUID in migration — use rolNombre pattern instead
        // For backward compat: if idRol equals the ADMIN role UUID it will be
        // resolved via the rolNombre stored in JWT. We store the role name in idRol field.
        String role = "ROLE_PROFESIONAL";
        if (idRol != null && (idRol.equals("ROLE_ADMIN") || idRol.equals("ADMIN"))) {
            role = "ROLE_ADMIN";
        }
        return List.of(new SimpleGrantedAuthority(role));
    }

    @Override
    public String getPassword() { return null; }

    @Override
    public boolean isAccountNonExpired() { return true; }

    @Override
    public boolean isAccountNonLocked() { return true; }

    @Override
    public boolean isCredentialsNonExpired() { return true; }

    @Override
    public boolean isEnabled() { return true; }

    /** Convenience method: true if this user has ROLE_ADMIN */
    public boolean isAdmin() {
        return getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    }
}
