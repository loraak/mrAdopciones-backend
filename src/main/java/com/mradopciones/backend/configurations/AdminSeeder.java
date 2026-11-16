package com.mradopciones.backend.configurations;

import com.mradopciones.backend.entities.Rol;
import com.mradopciones.backend.entities.Usuario;
import com.mradopciones.backend.repositories.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AdminSeeder implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.admin.username:}") private String username;
    @Value("${app.admin.email:}") private String email;
    @Value("${app.admin.password:}") private String password;

    @Override
    public void run(String... args) {
        if (username.isBlank() || email.isBlank() || password.isBlank()) return;
        if (usuarioRepository.existsByUsernameIgnoreCase(username)) return;

        Usuario admin = new Usuario();
        admin.setUsername(username.trim());
        admin.setCorreo(email.trim().toLowerCase());
        admin.setContrasenia(passwordEncoder.encode(password));
        admin.setRol(Rol.ADMIN);
        usuarioRepository.save(admin);
    }
}