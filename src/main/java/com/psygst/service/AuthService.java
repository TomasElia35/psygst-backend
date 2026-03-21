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
        Auth auth = authRepository.findByUsernameAndBaja(request.username(), (byte) 0)
                .orElseThrow(() -> new BadRequestException("Usuario no encontrado"));

        if (!auth.getActivo()) {
            throw new BadRequestException("Usuario inactivo");
        }

        if (!passwordEncoder.matches(request.password(), auth.getPassword())) {
            throw new BadRequestException("Contraseña incorrecta");
        }

        auth.setUltimoAcceso(LocalDateTime.now());
        authRepository.save(auth);

        Profesional prof = auth.getProfesional();
        String token = jwtProvider.generateToken(
                auth.getIdAuth(),
                prof != null ? prof.getIdProfesional() : null,
                prof != null && prof.getSistema() != null ? prof.getSistema().getIdSistema() : null,
                auth.getRol().getIdRol(),
                auth.getUsername());

        return new LoginResponse(
                token,
                auth.getUsername(),
                prof != null ? prof.getNombre() + " " + prof.getApellido() : "Admin",
                auth.getRol().getNombre());
    }

    @Transactional
    public void cambiarPassword(String username, String oldPassword, String newPassword) {
        Auth auth = authRepository.findByUsernameAndBaja(username, (byte) 0)
                .orElseThrow(() -> new BadRequestException("Usuario no encontrado"));

        if (!passwordEncoder.matches(oldPassword, auth.getPassword())) {
            throw new BadRequestException("Contraseña actual incorrecta");
        }

        auth.setPassword(passwordEncoder.encode(newPassword));
        authRepository.save(auth);
    }

    @Transactional
    public void register(RegisterRequest request) {
        if (authRepository.findByUsernameAndBaja(request.username(), (byte) 0).isPresent()) {
            throw new BadRequestException("El nombre de usuario ya está en uso");
        }

        Rol rol = rolRepository.findById(request.idRol())
                .orElseThrow(() -> new BadRequestException("Rol no encontrado"));

        Auth auth = Auth.builder()
                .username(request.username())
                .password(passwordEncoder.encode(request.password()))
                .rol(rol)
                .activo(true)
                .baja((byte) 0)
                .build();

        if ("PROFESIONAL".equals(rol.getNombre())) {
            Sistema sistema = Sistema.builder()
                    .nombre("Consultorio de " + request.nombre() + " " + request.apellido())
                    .activo(true)
                    .baja((byte) 0)
                    .build();
            sistema = sistemaRepository.save(sistema);

            Profesional profesional = Profesional.builder()
                    .uuid(java.util.UUID.randomUUID().toString())
                    .nombre(request.nombre())
                    .apellido(request.apellido())
                    .email(request.email())
                    .celular(request.celular())
                    .sistema(sistema)
                    .baja((byte) 0)
                    .build();
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
                            "Contraseña: (la que elegiste al registrarte)\n\n" +
                            "Puedes iniciar sesión en: https://psygst-frontend-djtu.vercel.app/login\n\n" +
                            "¡Éxitos!",
                    request.nombre(), request.username());
            try {
                emailService.enviar(request.email(), subject, body);
            } catch (Exception e) {
                // We don't want to fail registration if email fails, just log it
                System.err.println("Error enviando email de bienvenida: " + e.getMessage());
            }
        }
    }
}
