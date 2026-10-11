package com.mradopciones.backend.controllers;

import com.mradopciones.backend.configurations.RequiereSesion;
import com.mradopciones.backend.entities.DTOs.EstatusRequest;
import com.mradopciones.backend.entities.DTOs.SolicitudRequest;
import com.mradopciones.backend.entities.DTOs.SolicitudResponse;
import com.mradopciones.backend.entities.Rol;
import com.mradopciones.backend.services.SolicitudAdopcionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/solicitudes")
@RequiredArgsConstructor
public class SolicitudAdopcionController {

    private final SolicitudAdopcionService solicitudService;

    // ---------- Adoptante ----------

    @RequiereSesion(roles = Rol.ADOPTANTE)
    @PostMapping
    public ResponseEntity<SolicitudResponse> crear(@Valid @RequestBody SolicitudRequest request,
                                                   @RequestAttribute("userId") Long userId) {
        return ResponseEntity.status(HttpStatus.CREATED).body(solicitudService.crear(userId, request));
    }

    @RequiereSesion(roles = Rol.ADOPTANTE)
    @GetMapping("/mine")
    public List<SolicitudResponse> enviadas(@RequestAttribute("userId") Long userId) {
        return solicitudService.enviadas(userId);
    }

    // ---------- Donante ----------

    @RequiereSesion(roles = Rol.DONANTE)
    @GetMapping("/recibidas")
    public List<SolicitudResponse> recibidas(@RequestAttribute("userId") Long userId) {
        return solicitudService.recibidas(userId);
    }

    @RequiereSesion(roles = Rol.DONANTE)
    @PatchMapping("/{id}/estatus")
    public SolicitudResponse cambiarEstatus(@PathVariable Long id,
                                            @Valid @RequestBody EstatusRequest request,
                                            @RequestAttribute("userId") Long userId) {
        return solicitudService.cambiarEstatus(id, userId, request.estatus());
    }
}