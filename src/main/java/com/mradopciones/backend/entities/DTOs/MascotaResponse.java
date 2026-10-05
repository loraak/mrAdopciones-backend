package com.mradopciones.backend.entities.DTOs;

import com.mradopciones.backend.entities.Mascota;

import java.util.List;

public record MascotaResponse  (
        Long id,
        String nombre,
        Integer edad,
        String tipo,
        String descripcion,
        String imagen,
        String estatus,
        List<String> etiquetas
) {
    public static MascotaResponse from (Mascota m) {
        List<String> etiquetas = m.getEtiquetas() == null ? List.of() : List.copyOf(m.getEtiquetas());

        return new MascotaResponse(
                m.getId(), m.getNombre(), m.getEdad(), m.getTipo(), m.getDescripcion(), m.getImagen(), m.getEstatus(), etiquetas);
    }
}
