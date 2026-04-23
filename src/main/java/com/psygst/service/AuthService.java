package com.psygst.service;

import com.psygst.model.*;
import com.psygst.repository.*;
import com.psygst.security.JwtProvider;
import com.psygst.exception.BadRequestException;
import com.psygst.dto.auth.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthRepository authRepository;
    private final RolRepository rolRepository;
    private final ProfesionalRepository profesionalRepository;
    private final SistemaRepository sistemaRepository;
    private final EmailService emailService;
    private final JwtProvider jwtProvider;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public LoginResponse login(LoginRequest request) {
        Auth auth = authRepository.findByUsernameAndBaja(request.username(), false)
                .orElseThrow(() -> new BadRequestException("Usuario no encontrado"));

        if (!auth.getActivo()) {
            throw new BadRequestException("Usuario inactivo");
        }

        if (!passwordEncoder.matches(request.password(), auth.getPassword())) {
            throw new BadRequestException("ContraseÃ±a incorrecta");
        }

        auth.setUltimoAcceso(LocalDateTime.now());
        authRepository.save(auth);

        Profesional prof = auth.getProfesional();

        // idRol stored in JWT as the rol nombre (ROLE_ADMIN / ROLE_PROFESIONAL)
        // so JwtFilter can resolve the Spring Security role without a DB lookup
        String rolNombre = auth.getRol().getNombre(); // e.g. "ADMIN" or "PROFESIONAL"
        String tokenRolClaim = rolNombre.startsWith("ROLE_") ? rolNombre : "ROLE_" + rolNombre;

        String token = jwtProvider.generateToken(
                auth.getIdAuth(),
                prof != null ? prof.getIdProfesional() : null,
                prof != null && prof.getSistema() != null ? prof.getSistema().getIdSistema() : null,
                tokenRolClaim,   // e.g. "ROLE_ADMIN"
                auth.getUsername());

        return new LoginResponse(
                token,
                auth.getUsername(),
                prof != null ? prof.getNombre() + " " + prof.getApellido() : "Admin",
                auth.getRol().getNombre());
    }

    @Transactional
    public void cambiarPassword(String username, String oldPassword, String newPassword) {
        Auth auth = authRepository.findByUsernameAndBaja(username, false)
                .orElseThrow(() -> new BadRequestException("Usuario no encontrado"));

        if (!passwordEncoder.matches(oldPassword, auth.getPassword())) {
            throw new BadRequestException("ContraseÃ±a actual incorrecta");
        }

        auth.setPassword(passwordEncoder.encode(newPassword));
        authRepository.save(auth);
    }

    @Transactional
    public void register(RegisterRequest request) {
        if (authRepository.findByUsernameAndBaja(request.username(), false).isPresent()) {
            throw new BadRequestException("El nombre de usuario ya estÃ¡ en uso");
        }

        // idRol is now a UUID String
        Rol rol = rolRepository.findById(request.idRol())
                .orElseThrow(() -> new BadRequestException("Rol no encontrado"));

        Auth auth = Auth.builder()
                .username(request.username())
                .password(passwordEncoder.encode(request.password()))
                .rol(rol)
                .activo(true)
                .baja(false)
                .build();

        if ("PROFESIONAL".equals(rol.getNombre())) {
            Sistema sistema = Sistema.builder()
                    .nombre("Consultorio de " + request.nombre() + " " + request.apellido())
                    .activo(true)
                    .baja(false)
                    .build();
            sistema = sistemaRepository.save(sistema);

            Profesional profesional = Profesional.builder()
                    .nombre(request.nombre())
                    .apellido(request.apellido())
                    .email(request.email())
                    .celular(request.celular())
                    .sistema(sistema)
                    .baja(false)
                    .build();
            // idProfesional generated in @PrePersist
            profesional = profesionalRepository.save(profesional);
            auth.setProfesional(profesional);
        }

        authRepository.save(auth);

        // Send welcome email with credentials
        if (request.email() != null && !request.email().isBlank()) {
            String subject = "Bienvenido a PsyGst - Tus credenciales";
            String body = String.format(
                    "Hola %s!\n\nTu cuenta ha sido creada exitosamente.\n\n" +
                            "Tus credenciales de acceso son:\n" +
                            "Usuario: %s\n" +
                            "ContraseÃ±a: (la que elegiste al registrarte)\n\n" +
                            "Puedes iniciar sesiÃ³n en: https://psygst-frontend.vercel.app/login\n\n" +
                            "Â¡Ã‰xitos!",
                    request.nombre(), request.username());
            try {
                emailService.enviar(request.email(), subject, body);
            } catch (Exception e) {
                System.err.println("Error enviando email de bienvenida: " + e.getMessage());
            }
        }
    }
}
