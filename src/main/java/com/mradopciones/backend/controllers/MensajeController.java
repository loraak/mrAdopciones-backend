package com.mradopciones.backend.controllers;

import com.mradopciones.backend.configurations.RequiereSesion;
import com.mradopciones.backend.entities.DTOs.ChatInfoResponse;
import com.mradopciones.backend.entities.DTOs.MensajeRequest;
import com.mradopciones.backend.entities.DTOs.MensajeResponse;
import com.mradopciones.backend.services.MensajeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// Cualquier usuario con sesión puede llamar; el servicio verifica que participe en la solicitud
@RequiereSesion
@RestController
@RequestMapping("/api/requests/{solicitudId}")
@RequiredArgsConstructor
public class MensajeController {

    private final MensajeService mensajeService;

    /** Datos del encabezado: con quién hablas y sobre qué mascota. */
    @GetMapping("/chat")
    public ChatInfoResponse info(@PathVariable Long solicitudId,
                                 @RequestAttribute("userId") Long userId) {
        return mensajeService.info(solicitudId, userId);
    }

    /** GET /messages?after=12 devuelve solo los mensajes con id mayor a 12. */
    @GetMapping("/messages")
    public List<MensajeResponse> listar(@PathVariable Long solicitudId,
                                        @RequestParam(defaultValue = "0") long after,
                                        @RequestAttribute("userId") Long userId) {
        return mensajeService.listar(solicitudId, userId, after);
    }

    @PostMapping("/messages")
    public ResponseEntity<MensajeResponse> enviar(@PathVariable Long solicitudId,
                                                  @Valid @RequestBody MensajeRequest request,
                                                  @RequestAttribute("userId") Long userId) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(mensajeService.enviar(solicitudId, userId, request.contenido()));
    }

    /** "Limpiar historial": oculta los mensajes actuales solo para quien lo pide. */
    @PostMapping("/messages/clear")
    public ResponseEntity<Void> limpiar(@PathVariable Long solicitudId,
                                        @RequestAttribute("userId") Long userId) {
        mensajeService.limpiarHistorial(solicitudId, userId);
        return ResponseEntity.noContent().build();
    }
}