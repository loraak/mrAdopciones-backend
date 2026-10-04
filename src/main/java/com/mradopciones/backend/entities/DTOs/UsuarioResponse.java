package com.mradopciones.backend.entities.DTOs;

import com.mradopciones.backend.entities.Rol;
import com.mradopciones.backend.entities.Usuario;

public record UsuarioResponse (
    Long id,
    String username,
    String correo,
    Rol rol,
    String telefono,
    String ocupacion,
    String locacion,
    String organizacion
) {
    public static UsuarioResponse from (Usuario u) {
        return new UsuarioResponse(
                u.getId(), u.getUsername(), u.getCorreo(), u.getRol(), u.getTelefono(), u.getOcupacion(), u.getLocacion(), u.getOrganizacion()
        );
    }
}