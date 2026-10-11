package com.mradopciones.backend.entities.DTOs;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record SolicitudRequest(
        @NotNull(message = "La mascota es obligatoria")
        Long mascotaId,

        @NotBlank(message = "El mensaje es obligatorio")
        @Size(min = 10, max = 1000, message = "El mensaje debe tener entre 10 y 1000 caracteres")
        String mensaje
) {}
