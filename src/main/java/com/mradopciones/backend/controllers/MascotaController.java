package com.mradopciones.backend.controllers;

import com.mradopciones.backend.configurations.RequiereSesion;
import com.mradopciones.backend.entities.DTOs.MascotaRequest;
import com.mradopciones.backend.entities.DTOs.MascotaResponse;
import com.mradopciones.backend.entities.Rol;
import com.mradopciones.backend.services.MascotaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/mascotas")
@RequiredArgsConstructor
public class MascotaController {

    private final MascotaService mascotaService;

    @GetMapping
    public List<MascotaResponse> catalogo(@RequestParam(required = false) String tipo) {
        return mascotaService.getCatalogo(tipo);
    }

    @GetMapping("/{id}")
    public MascotaResponse getMascotaById(@PathVariable Long id) {
        return mascotaService.getMascotaById(id);
    }

    // Donantes

    @RequiereSesion(roles = Rol.DONANTE)
    @GetMapping("/mine")
    public List<MascotaResponse> misMascotas(@RequestAttribute("userId") Long userId) {
        return mascotaService.getMisMascotas(userId);
    }

    @RequiereSesion(roles = Rol.DONANTE)
    @PostMapping
    public ResponseEntity<MascotaResponse> crear(@Valid @RequestBody MascotaRequest request,
                                                 @RequestAttribute("userId") Long userId) {
        return ResponseEntity.status(HttpStatus.CREATED).body(mascotaService.crear(userId, request));
    }

    @RequiereSesion(roles = Rol.DONANTE)
    @PutMapping("/{id}")
    public MascotaResponse actualizar(@PathVariable Long id,
                                      @Valid @RequestBody MascotaRequest request,
                                      @RequestAttribute("userId") Long userId) {
        return mascotaService.actualizar(id, userId, request);
    }

    @RequiereSesion(roles = Rol.DONANTE)
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id,
                                         @RequestAttribute("userId") Long userId) {
        mascotaService.eliminar(id, userId);
        return ResponseEntity.noContent().build();
    }
}