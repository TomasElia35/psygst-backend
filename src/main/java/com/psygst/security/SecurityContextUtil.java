package com.psygst.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Utility class to access the current authenticated user's tenant context.
 * RN-S01: services MUST always call these methods — never trust request body
 * for tenant data.
 * 
 * Post UUID migration: all IDs return String (UUID format).
 */
public class SecurityContextUtil {

    public static PsygstUserDetails getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof PsygstUserDetails)) {
            throw new IllegalStateException("No authenticated user found in SecurityContext");
        }
        return (PsygstUserDetails) auth.getPrincipal();
    }

    public static String getCurrentIdSistema() {
        return getCurrentUser().getIdSistema();
    }

    public static String getCurrentIdProfesional() {
        return getCurrentUser().getIdProfesional();
    }

    public static String getCurrentIdAuth() {
        return getCurrentUser().getIdAuth();
    }

    public static boolean isAdmin() {
        return getCurrentUser().isAdmin();
    }
}
