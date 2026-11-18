package com.mradopciones.backend.entities.DTOs;

import com.mradopciones.backend.entities.Rol;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record RegistroRequest (
        @NotBlank(message = "El nombre de usuario es obligatorio")
        @Size(min = 3, max = 30, message = "El usuario debe tener entre 3 y 30 caracteres")   // CORRECCIÓN D1
        String username,

        @NotBlank
        @Email(message = "El correo no es válido")
        String correo,

        @NotBlank(message = "La contraseña es obligatoria")
        @Size(min = 8, max = 72, message = "La contraseña debe tener entre 8 y 72 caracteres")
        String contrasena,

        @NotNull(message = "El rol es obligatorio")
        Rol rol,

        String telefono,
        String ocupacion,

        String locacion,
        String organizacion
) {}