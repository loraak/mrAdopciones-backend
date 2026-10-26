package com.mradopciones.backend.entities.DTOs;

import com.mradopciones.backend.entities.Mensaje;

import java.time.LocalDateTime;
import java.util.Objects;

public record MensajeResponse(
        Long id,
        String emisorNombre,
        boolean propio,        // true si lo escribió quien hace la consulta
        String contenido,
        LocalDateTime fecha
) {
    public static MensajeResponse from(Mensaje m, Long userId) {
        return new MensajeResponse(
                m.getId(),
                m.getEmisor().getUsername(),
                Objects.equals(m.getEmisor().getId(), userId),
                m.getContenido(),
                m.getFechaEnvio()
        );
    }
}