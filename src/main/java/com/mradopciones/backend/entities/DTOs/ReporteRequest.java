package com.mradopciones.backend.entities.DTOs;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

// ReporteRequest.java
public record ReporteRequest(
        @NotBlank(message = "El tipo es obligatorio")
        @Pattern(regexp = "TECNICO|USUARIO", message = "El tipo debe ser TECNICO o USUARIO")
        String tipo,

        @NotBlank(message = "Describe el problema")
        @Size(min = 10, max = 500, message = "La descripción debe tener entre 10 y 500 caracteres")
        String descripcion
) {}