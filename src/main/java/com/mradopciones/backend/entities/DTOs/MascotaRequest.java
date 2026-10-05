package com.mradopciones.backend.entities.DTOs;

import jakarta.validation.constraints.*;

import java.util.List;

public record MascotaRequest(
        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 100, message = "El nombre no puede pasar de 100 caracteres")
        String nombre,

        @NotNull(message = "La edad es obligatoria")
        @Min(value = 0, message = "La edad no puede ser negativa")
        @Max(value = 50, message = "La edad no es válida")
        Integer edad,

        @NotBlank(message = "El tipo es obligatorio")
        @Pattern(regexp = "perros|gatos|otros", message = "El tipo debe ser perros, gatos u otros")
        String tipo,

        @NotBlank(message = "La descripción es obligatoria")
        @Size(max = 2000, message = "La descripción no puede pasar de 2000 caracteres")
        String descripcion,

        @Size(max = 255, message = "La URL de la imagen es demasiado larga")
        String imagen,

        @Pattern(regexp = "Disponible|En proceso|Adoptado", message = "Estatus inválido")
        String estatus,

        @Size(max = 10, message = "Máximo 10 etiquetas")
        List<@NotBlank(message = "Las etiquetas no pueden estar vacías")
        @Size(max = 30, message = "Cada etiqueta puede tener máximo 30 caracteres") String> etiquetas
) {}