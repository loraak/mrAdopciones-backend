package com.mradopciones.backend.services;

import com.mradopciones.backend.entities.DTOs.RegistroRequest;
import com.mradopciones.backend.entities.Rol;
import com.mradopciones.backend.entities.Usuario;
import com.mradopciones.backend.repositories.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class UsuarioService {
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public Usuario registrar(RegistroRequest req) {
        String username = req.username().trim();
        String correo = req.correo().trim().toLowerCase();

        // HttpClient trata cualquier código 4xx como error y ejecuta el bloque error.
        if (usuarioRepository.existsByCorreoIgnoreCase(correo)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El correo ya está registrado");
        }
        if (usuarioRepository.existsByUsernameIgnoreCase(username)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El nombre de usuario ya está en uso");
        }

        Usuario usuario = new Usuario();
        usuario.setUsername(username);
        usuario.setCorreo(correo);
        usuario.setContrasenia(passwordEncoder.encode(req.contrasena()));
        usuario.setRol(req.rol());

        if (req.rol() == Rol.ADOPTANTE) {
            if (esVacio(req.telefono())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El número de celular es obligatorio para adoptantes");
            }
            usuario.setTelefono(req.telefono().trim());
            usuario.setOcupacion(limpiar(req.ocupacion()));
        } else {
            usuario.setLocacion(limpiar(req.locacion()));
            usuario.setOrganizacion(limpiar(req.organizacion()));
        }
        return usuarioRepository.save(usuario);
    }

    private boolean esVacio(String s) {
        return s == null || s.isBlank();
    }

    private String limpiar(String s) {
        return esVacio(s) ? null : s.trim();
    }
}
