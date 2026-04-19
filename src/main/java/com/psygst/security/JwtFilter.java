package com.psygst.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * JWT filter — post UUID migration.
 * All ID claims are now String. Tokens issued before the migration
 * will fail claim extraction (null) and be treated as unauthenticated,
 * forcing re-login.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class JwtFilter extends OncePerRequestFilter {

    private final JwtProvider jwtProvider;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7);

        if (!jwtProvider.validateToken(token)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        String idSistema     = jwtProvider.extractIdSistema(token);
        String idProfesional = jwtProvider.extractIdProfesional(token);
        String idRol         = jwtProvider.extractIdRol(token);
        String idAuth        = jwtProvider.extractIdAuth(token);
        String username      = jwtProvider.extractUsername(token);

        // idRol holds the role name string (ROLE_ADMIN / ROLE_PROFESIONAL)
        String roleAuthority = (idRol != null && (idRol.equals("ROLE_ADMIN") || idRol.equals("ADMIN")))
                ? "ROLE_ADMIN" : "ROLE_PROFESIONAL";

        PsygstUserDetails userDetails = new PsygstUserDetails(
                idAuth, idProfesional, idSistema, idRol, username);

        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                userDetails, null, List.of(new SimpleGrantedAuthority(roleAuthority)));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        filterChain.doFilter(request, response);
    }
}
