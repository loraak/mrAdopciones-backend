package com.mradopciones.backend.entities.DTOs;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record MensajeRequest(
        @NotBlank(message = "Escribe un mensaje")
        @Size(max = 500, message = "El mensaje no puede pasar de 500 caracteres")
        String contenido
) {}