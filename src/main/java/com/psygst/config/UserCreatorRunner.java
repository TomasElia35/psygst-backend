package com.psygst.config;

import com.psygst.model.Auth;
import com.psygst.model.Rol;
import com.psygst.repository.AuthRepository;
import com.psygst.repository.RolRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.env.Environment;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Slf4j
public class UserCreatorRunner implements CommandLineRunner {

    private final AuthRepository authRepository;
    private final RolRepository rolRepository;
    private final PasswordEncoder passwordEncoder;
    private final Environment env;

    @Override
    @Transactional
    public void run(String... args) {
        String username = env.getProperty("psygst.usuario");
        String password = env.getProperty("psygst.password");
        
        if (username != null && password != null) {
            log.info("========================================");
            log.info("Iniciando script de creaciÃ³n de usuario para: {}", username);
            
            if (authRepository.findByUsername(username).isPresent()) {
                log.warn("El usuario '{}' ya existe en la base de datos. Saltando creaciÃ³n.", username);
                log.info("========================================");
                return;
            }

            Rol rolAdmin = rolRepository.findByNombre("ROLE_ADMIN")
                    .orElseGet(() -> rolRepository.save(Rol.builder().nombre("ROLE_ADMIN").baja(false).build()));

            Auth newAuth = Auth.builder()
                    .username(username)
                    .password(passwordEncoder.encode(password))
                    .rol(rolAdmin)
                    .activo(true)
                    .baja(false)
                    .build();

            authRepository.save(newAuth);
            log.info("âœ… Usuario '{}' creado exitosamente con contraseÃ±a encriptada.", username);
            log.info("========================================");
        }
    }
}
