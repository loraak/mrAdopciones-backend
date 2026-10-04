package com.mradopciones.backend.repositories;

import com.mradopciones.backend.entities.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    boolean existsByCorreoIgnoreCase(String correo);

    boolean existsByUsernameIgnoreCase(String username);

    Optional<Usuario> findByCorreoIgnoreCase(String correo);
}
