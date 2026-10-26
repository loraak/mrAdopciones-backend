package com.mradopciones.backend.entities.DTOs;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LoginRequest(
        @NotBlank(message = "El usuario es obligatorio")
        @Size(max = 100, message = "El usuario es demasiado largo")
        String username,

        @NotBlank(message = "La contraseña es obligatoria")
        @Size(max = 72, message = "La contraseña es demasiado larga")
        String contrasena
) {}