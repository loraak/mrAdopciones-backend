package com.mradopciones.backend.entities.DTOs;

import com.mradopciones.backend.entities.Mascota;
import com.mradopciones.backend.entities.SolicitudAdopcion;
import com.mradopciones.backend.entities.Usuario;

import java.time.LocalDateTime;

public record SolicitudResponse(
        Long id,
        Long mascotaId,
        String mascotaNombre,
        String solicitanteNombre,
        String email,
        String telefono,
        String ubicacion,
        String mensaje,
        String estatus,
        LocalDateTime fecha
) {
    public static SolicitudResponse from(SolicitudAdopcion s) {
        Mascota m = s.getMascota();
        Usuario u = s.getAdoptante();
        return new SolicitudResponse(
                s.getId(), m.getId(), m.getNombre(),
                u.getUsername(), u.getCorreo(), u.getTelefono(), u.getLocacion(),
                s.getMensaje(), s.getEstatus(), s.getFechaCreacion()
        );
    }
}
