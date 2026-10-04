package com.mradopciones.backend.controllers;

import com.mradopciones.backend.entities.DTOs.RegistroRequest;
import com.mradopciones.backend.entities.DTOs.UsuarioResponse;
import com.mradopciones.backend.entities.Usuario;
import com.mradopciones.backend.services.UsuarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
