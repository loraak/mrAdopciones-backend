package com.mradopciones.backend.entities.DTOs;

import com.mradopciones.backend.entities.Reporte;

import java.time.LocalDateTime;

// ReporteResponse.java
public record ReporteResponse(
        Long id,
        Long solicitudId,
        String mascotaNombre,
        String tipo,
        String reportanteNombre,
        String reportadoNombre,   // null en reportes técnicos
        String descripcion,
        String estatus,
        LocalDateTime fecha
) {
    public static ReporteResponse from(Reporte r) {
        return new ReporteResponse(
                r.getId(),
                r.getSolicitud().getId(),
                r.getSolicitud().getMascota().getNombre(),
                r.getTipo(),
                r.getReportante().getUsername(),
                r.getReportado() == null ? null : r.getReportado().getUsername(),
                r.getDescripcion(),
                r.getEstatus(),
                r.getFechaCreacion()
        );
    }
}