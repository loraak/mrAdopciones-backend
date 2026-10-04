package com.mradopciones.backend.controllers;

import com.mradopciones.backend.entities.DTOs.LoginRequest;
import com.mradopciones.backend.entities.DTOs.LoginResponse;
import com.mradopciones.backend.entities.DTOs.RegistroRequest;
import com.mradopciones.backend.entities.DTOs.UsuarioResponse;
import com.mradopciones.backend.entities.Usuario;
import com.mradopciones.backend.services.UsuarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final UsuarioService usuarioService;

    @PostMapping("/registro")
    public ResponseEntity<UsuarioResponse> registro(@Valid @RequestBody RegistroRequest request) {
        Usuario creado = usuarioService.registrar(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(UsuarioResponse.from(creado));
    }

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        return usuarioService.login(request);
    }

    //Rutas protegidas.
    @GetMapping("/me")
    public UsuarioResponse me(@RequestAttribute("userId") Long userId) {
        return UsuarioResponse.from(usuarioService.obtenerporId(userId));
    }
}
