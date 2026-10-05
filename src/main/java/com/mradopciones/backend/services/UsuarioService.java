package com.mradopciones.backend.services;

import com.mradopciones.backend.entities.DTOs.LoginRequest;
import com.mradopciones.backend.entities.DTOs.LoginResponse;
import com.mradopciones.backend.entities.DTOs.RegistroRequest;
import com.mradopciones.backend.entities.DTOs.UsuarioResponse;
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
    private final JWTService jwtService;

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
            if (esVacio(req.ocupacion())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La ocupación es obligatoria para adoptantes");
            }
            usuario.setTelefono(req.telefono().trim());
            usuario.setOcupacion(limpiar(req.ocupacion()));
        } else {
            usuario.setLocacion(limpiar(req.locacion()));
            usuario.setOrganizacion(limpiar(req.organizacion()));
        }
        return usuarioRepository.save(usuario);
    }

    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest req) {
        String identificador = req.username().trim();

        Usuario usuario = usuarioRepository.findByUsernameIgnoreCase(identificador)
                .or(() -> usuarioRepository.findByCorreoIgnoreCase(identificador))
                .orElseThrow(this::credencialesInvalidas);

        if (!passwordEncoder.matches(req.contrasena(), usuario.getContrasenia())) {
            throw credencialesInvalidas();
        }

        return new LoginResponse(jwtService.generarToken(usuario), UsuarioResponse.from(usuario));
    }

    @Transactional(readOnly = true)
    public Usuario obtenerPorId(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Sesión inválida"));
    }

    private ResponseStatusException credencialesInvalidas() {
        return new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuario o contraseña incorrectos");
    }

    private boolean esVacio(String s) {
        return s == null || s.isBlank();
    }

    private String limpiar(String s) {
        return esVacio(s) ? null : s.trim();
    }
}
