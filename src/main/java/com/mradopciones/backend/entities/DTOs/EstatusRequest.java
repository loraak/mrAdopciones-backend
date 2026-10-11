package com.mradopciones.backend.entities.DTOs;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record EstatusRequest(
        @NotBlank(message = "El estatus es obligatorio")
        @Pattern(regexp = "Aceptada|Rechazada", message = "El estatus debe ser Aceptada o Rechazada")
        String estatus
) {}
