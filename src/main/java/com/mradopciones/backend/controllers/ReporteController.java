package com.mradopciones.backend.controllers;

import com.mradopciones.backend.configurations.RequiereSesion;
import com.mradopciones.backend.entities.DTOs.ReporteRequest;
import com.mradopciones.backend.entities.DTOs.ReporteResponse;
import com.mradopciones.backend.entities.Rol;
import com.mradopciones.backend.services.ReporteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class ReporteController {

    private final ReporteService reporteService;

    // Cualquier usuario con sesión; el servicio verifica que participe en el chat
    @RequiereSesion
    @PostMapping("/api/requests/{solicitudId}/reports")
    public ResponseEntity<ReporteResponse> crear(@PathVariable Long solicitudId,
                                                 @Valid @RequestBody ReporteRequest request,
                                                 @RequestAttribute("userId") Long userId) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(reporteService.crear(solicitudId, userId, request));
    }

    @RequiereSesion(roles = Rol.ADMIN)
    @GetMapping("/api/reportes")
    public List<ReporteResponse> listar() {
        return reporteService.listar();
    }

    @RequiereSesion(roles = Rol.ADMIN)
    @PatchMapping("/api/reportes/{id}/revisado")
    public ReporteResponse marcarRevisado(@PathVariable Long id) {
        return reporteService.marcarRevisado(id);
    }
}